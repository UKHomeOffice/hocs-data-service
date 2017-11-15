package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.digital.ho.hocs.dto.legacy.units.UnitCreateRecord;
import uk.gov.digital.ho.hocs.dto.legacy.units.UnitRecord;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.legacy.units.CSVGroupLine;
import uk.gov.digital.ho.hocs.legacy.units.UnitFileParser;
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
            List<BusinessGroup> list = repo.findAll();
            return UnitRecord.create(list);
        } catch (NullPointerException e) {
            throw new ListNotFoundException();
        }
    }

    public BusinessGroup getGroupByReference(String referenceName) throws ListNotFoundException {
        try {
            BusinessGroup businessGroup = repo.findByReferenceName(referenceName);
            return businessGroup;
        } catch (NullPointerException e) {
            throw new ListNotFoundException();
        }
    }

    @CacheEvict(value = "groups", allEntries = true)
    public void createGroupsFromCSV(MultipartFile file) {
        Set<CSVGroupLine> lines = new UnitFileParser(file).getLines();

        Map<String, Set<BusinessGroup>> groupMap = new HashMap<>();
        for (CSVGroupLine line : lines) {
            groupMap.putIfAbsent(line.getUnit(), new HashSet<>());
            groupMap.get(line.getUnit()).add(new BusinessGroup(line.getTeam(), line.getTeamValue()));
        }

        Set<BusinessGroup> groups = new HashSet<>();
        for (Map.Entry<String, Set<BusinessGroup>> entity : groupMap.entrySet()) {
            BusinessGroup group = new BusinessGroup(entity.getKey());
            group.setSubGroups(entity.getValue());
            groups.add(group);
        }

        createGroups(groups);
    }

    public UnitCreateRecord getGroupsCreateList() throws ListNotFoundException {
        try {
            List<BusinessGroup> list = repo.findAll();
            return UnitCreateRecord.create(list);
        } catch (NullPointerException e) {
            throw new ListNotFoundException();
        }
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