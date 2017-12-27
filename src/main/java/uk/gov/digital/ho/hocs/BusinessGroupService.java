package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.dto.legacy.units.UnitCreateRecord;
import uk.gov.digital.ho.hocs.dto.legacy.units.UnitRecord;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.GroupCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.ingest.units.CSVGroupLine;
import uk.gov.digital.ho.hocs.model.BusinessGroup;

import java.util.*;

@Service
@Slf4j
public class BusinessGroupService {
    private final BusinessGroupRepository repo;

    @Autowired
    public BusinessGroupService(BusinessGroupRepository repo) {
        this.repo = repo;
    }

    @Cacheable(value = "groups")
    public UnitRecord getAllGroups() throws ListNotFoundException {
        try {
            Set<BusinessGroup> list = repo.findAll();
            return UnitRecord.create(list);
        } catch (NullPointerException e) {
            throw new ListNotFoundException();
        }
    }

    @Cacheable(value = "groups", key = "#referenceName")
    public BusinessGroup getGroupByReference(String referenceName) throws ListNotFoundException {
        try {
            BusinessGroup businessGroup = repo.findByReferenceName(referenceName);
            return businessGroup;
        } catch (NullPointerException e) {
            throw new ListNotFoundException();
        }
    }

    @CacheEvict(value = "groups", allEntries = true)
    public void createGroupsFromCSV(Set<CSVGroupLine> lines) {
        Set<BusinessGroup> groups = getGroups(lines);
        if (!groups.isEmpty()) {
            createGroups(new HashSet<>(groups));
        }
    }

    @CacheEvict(value = "groups", allEntries = true)
    public void updateGroupsFromCSV(Set<CSVGroupLine> lines) {
        Set<BusinessGroup> newGroups = getGroups(lines);
        Set<BusinessGroup> jpaGroups = repo.findAll();

        jpaGroups.forEach(t -> {
            Optional<BusinessGroup> currentBusinessGroup = newGroups.stream().filter(i -> i.equals(t)).findFirst();
            if (currentBusinessGroup.isPresent()) {
                t.getSubGroups().forEach(s -> {
                    s.setDeleted(!currentBusinessGroup.get().getSubGroups().contains(s));
                });
            } else {
                t.getSubGroups().forEach(s -> s.setDeleted(true));
            }

            t.setDeleted(t.getSubGroups().stream().allMatch(topic -> topic.getDeleted()));
        });

        newGroups.forEach(t -> {

            if (!jpaGroups.contains(t)) {
                jpaGroups.add(t);
            }
        });

        createGroups(jpaGroups);
    }

    @Caching( evict = {@CacheEvict(value = "groups", allEntries = true)})
    public void clearCache(){
        log.info("All groups cache cleared");
    }

    private void validateLine(CSVGroupLine line) throws GroupCreationException {
        if (line.getTeamValue().length() > 94) {
            throw new GroupCreationException("Group name exceeds size limit");
        }
    }

    public UnitCreateRecord getGroupsCreateList() throws ListNotFoundException {
        try {
            Set<BusinessGroup> list = repo.findAll();
            return UnitCreateRecord.create(list);
        } catch (NullPointerException e) {
            throw new ListNotFoundException();
        }
    }

    private Set<BusinessGroup> getGroups(Set<CSVGroupLine> lines) {
        Map<String, BusinessGroup> units = new HashMap<>();
        try {
            for (CSVGroupLine line : lines) {
                units.putIfAbsent(line.getUnitReference(), new BusinessGroup(line.getUnitDisplay(), line.getUnitReference()));
                BusinessGroup unit = units.get(line.getUnitReference());
                unit.getSubGroups().add(new BusinessGroup(line.getTeamReference(), line.getTeamValue()));
                validateLine(line);
            }

        } catch (GroupCreationException e) {
            e.printStackTrace();
        }
        return new HashSet<>(units.values());
    }

    private void createGroups(Set<BusinessGroup> groups) {
        try {
            repo.save(groups);
        } catch (DataIntegrityViolationException e) {

            if (e.getCause() instanceof ConstraintViolationException &&
                    ((ConstraintViolationException) e.getCause()).getConstraintName().toLowerCase().contains("group_name_ref_idempotent")) {
                throw new EntityCreationException("Identified an attempt to recreate existing entity, rolling back");
            }

            throw e;
        }
    }
}