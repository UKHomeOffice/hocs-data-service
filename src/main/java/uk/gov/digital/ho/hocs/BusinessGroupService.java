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
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.GroupCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.ingest.units.CSVBusinessGroupLine;
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

    public UnitCreateRecord getGroupsCreateList() throws ListNotFoundException {
        try {
            Set<BusinessGroup> list = repo.findAll();
            return UnitCreateRecord.create(list);
        } catch (NullPointerException e) {
            throw new ListNotFoundException();
        }
    }

    @Cacheable(value = "groups", key = "#referenceName")
    public BusinessGroup getGroupByReference(String referenceName) throws ListNotFoundException {
        BusinessGroup businessGroup = repo.findOneByReferenceNameAndDeletedIsFalse(referenceName);
        if(businessGroup == null)
        {
            throw new ListNotFoundException();
        }
        return businessGroup;
    }

    @Cacheable(value = "groups", key = "all")
    public Set<BusinessGroup> getAllBusinessGroups() throws ListNotFoundException {
        Set<BusinessGroup> list = repo.findAllByDeletedIsFalse();
        if (list.isEmpty()) {
            throw new ListNotFoundException();
        }
        return list;
    }

    @CacheEvict(value = "groups", allEntries = true)
    public void updateBusinessGroups(Set<CSVBusinessGroupLine> lines) throws GroupCreationException {
        if(lines != null) {
            List<BusinessGroup> newGroups = buildBusinessGroups(lines);
            Set<BusinessGroup> jpaGroups = repo.findAll();

            jpaGroups.forEach(jpaGroup -> {

                if (newGroups.contains(jpaGroup)) {
                    BusinessGroup matchingNewBusinessGroup = newGroups.get(newGroups.indexOf(jpaGroup));

                    Set<BusinessGroup> newBusinessGroupItems = matchingNewBusinessGroup.getSubGroups();
                    Set<BusinessGroup> jpaBusinessGroupItems = jpaGroup.getSubGroups();

                    // Update existing business groups
                    jpaBusinessGroupItems.forEach(item -> {
                        item.setDeleted(!newBusinessGroupItems.contains(item));
                    });

                    // Add new business groups
                    newBusinessGroupItems.forEach(newTopic -> {
                        if (!jpaBusinessGroupItems.contains(newTopic)) {
                            jpaBusinessGroupItems.add(newTopic);
                        }
                    });

                    jpaGroup.setSubGroups(jpaBusinessGroupItems);

                    // Set the topic group to deleted if there are no visible topic items
                    jpaGroup.setDeleted(jpaGroup.getSubGroups().stream().allMatch(topic -> topic.getDeleted()));

                } else {
                    jpaGroup.getSubGroups().forEach(item -> item.setDeleted(true));
                    jpaGroup.setDeleted(true);
                }
            });

            // Add new topic groups
            newGroups.forEach(newTopicGroup -> {
                if (!jpaGroups.contains(newTopicGroup)) {
                    jpaGroups.add(newTopicGroup);
                }
            });

            saveGroups(jpaGroups);
        } else{
            throw new EntityCreationException("Unable to update entity");
        }

    }

    @Caching( evict = {@CacheEvict(value = "groups", allEntries = true)})
    public void clearCache(){
        log.info("All groups cache cleared");
    }

    private static List<BusinessGroup> buildBusinessGroups(Set<CSVBusinessGroupLine> lines) throws GroupCreationException {
        Map<BusinessGroup, Set<BusinessGroup>> units = new HashMap<>();
        for (CSVBusinessGroupLine line : lines) {
            BusinessGroup unit = new BusinessGroup(line.getUnitDisplay(), line.getUnitReference());
            units.putIfAbsent(unit, new HashSet<>());
            units.get(unit).add(new BusinessGroup(line.getTeamReference(), line.getTeamValue()));
        }

        List<BusinessGroup> businessGroups = new ArrayList<>();
        for(Map.Entry<BusinessGroup, Set<BusinessGroup>> entity : units.entrySet()) {
            BusinessGroup unit = entity.getKey();
            unit.setSubGroups(entity.getValue());
            businessGroups.add(unit);
        }
        return businessGroups;
    }

    private void saveGroups(Collection<BusinessGroup> groups) {
        try {
            if(!groups.isEmpty()) {
                repo.save(groups);
            }
        } catch (DataIntegrityViolationException e) {

            if (e.getCause() instanceof ConstraintViolationException &&
                    ((ConstraintViolationException) e.getCause()).getConstraintName().toLowerCase().contains("group_name_ref_idempotent")) {
                throw new EntityCreationException("Identified an attempt to recreate existing entity, rolling back");
            }

            throw e;
        }
    }
}