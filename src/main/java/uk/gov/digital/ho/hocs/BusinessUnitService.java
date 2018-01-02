package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.dto.units.PublishUnitRecord;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.GroupCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.ingest.units.CSVBusinessGroupLine;
import uk.gov.digital.ho.hocs.model.BusinessTeam;
import uk.gov.digital.ho.hocs.model.BusinessUnit;

import java.util.*;

@Service
@Slf4j
public class BusinessUnitService {
    private final BusinessUnitRepository unitsRepo;
    private final BusinessTeamRepository teamsRepo;

    @Autowired
    public BusinessUnitService(BusinessUnitRepository unitsRepo, BusinessTeamRepository teamsRepo) {
        this.unitsRepo = unitsRepo;
        this.teamsRepo = teamsRepo;
    }

    public PublishUnitRecord getGroupsCreateList() throws ListNotFoundException {
        try {
            Set<BusinessUnit> list = unitsRepo.findAll();
            return PublishUnitRecord.create(list);
        } catch (NullPointerException e) {
            throw new ListNotFoundException();
        }
    }

    @Cacheable(value = "teams", key = "#referenceName")
    public BusinessTeam getTeamByReference(String referenceName) throws ListNotFoundException {
        BusinessTeam businessTeam = teamsRepo.findOneByReferenceNameAndDeletedIsFalse(referenceName);
        if(businessTeam == null)
        {
            throw new ListNotFoundException();
        }
        return businessTeam;
    }

    @Cacheable(value = "units", key = "all")
    public Set<BusinessUnit> getAllBusinessUnits() throws ListNotFoundException {
        Set<BusinessUnit> list = unitsRepo.findAllByDeletedIsFalse();
        if (list.isEmpty()) {
            throw new ListNotFoundException();
        }
        return list;
    }

    @Caching( evict = {@CacheEvict(value = "units", allEntries = true),
                       @CacheEvict(value = "teams", allEntries = true)})
    public void updateBusinessUnits(Set<CSVBusinessGroupLine> lines) throws GroupCreationException {
        if(lines != null) {
            List<BusinessUnit> newUnits = buildBusinessUnits(lines);
            Set<BusinessUnit> jpaUnits = unitsRepo.findAll();

            jpaUnits.forEach(jpaUnit -> {

                if (newUnits.contains(jpaUnit)) {
                    BusinessUnit matchingNewBusinessUnit = newUnits.get(newUnits.indexOf(jpaUnit));

                    Set<BusinessTeam> newBusinessTeams = matchingNewBusinessUnit.getTeams();
                    Set<BusinessTeam> jpaBusinessTeams = jpaUnit.getTeams();

                    // Update existing business teams
                    jpaBusinessTeams.forEach(item -> {
                        item.setDeleted(!newBusinessTeams.contains(item));
                    });

                    // Add new business teams
                    newBusinessTeams.forEach(newTopic -> {
                        if (!jpaBusinessTeams.contains(newTopic)) {
                            jpaBusinessTeams.add(newTopic);
                        }
                    });

                    jpaUnit.setTeams(jpaBusinessTeams);

                    // Set the topic group to deleted if there are no visible topic items
                    jpaUnit.setDeleted(jpaUnit.getTeams().stream().allMatch(topic -> topic.getDeleted()));

                } else {
                    jpaUnit.getTeams().forEach(item -> item.setDeleted(true));
                    jpaUnit.setDeleted(true);
                }
            });

            // Add new topic teams
            newUnits.forEach(newTopicGroup -> {
                if (!jpaUnits.contains(newTopicGroup)) {
                    jpaUnits.add(newTopicGroup);
                }
            });

            saveUnits(jpaUnits);
        } else{
            throw new EntityCreationException("Unable to update entity");
        }

    }

    @Caching( evict = {@CacheEvict(value = "units", allEntries = true),
                       @CacheEvict(value = "teams", allEntries = true)})
    public void clearCache(){
        log.info("All teams cache cleared");
    }

    private static List<BusinessUnit> buildBusinessUnits(Set<CSVBusinessGroupLine> lines) throws GroupCreationException {
        Map<BusinessUnit, Set<BusinessTeam>> units = new HashMap<>();
        for (CSVBusinessGroupLine line : lines) {
            BusinessUnit unit = new BusinessUnit(line.getUnitDisplay(), line.getUnitReference());
            units.putIfAbsent(unit, new HashSet<>());
            units.get(unit).add(new BusinessTeam(line.getTeamReference(), line.getTeamValue()));
        }

        List<BusinessUnit> businessUnits = new ArrayList<>();
        for(Map.Entry<BusinessUnit, Set<BusinessTeam>> entity : units.entrySet()) {
            BusinessUnit unit = entity.getKey();
            unit.setTeams(entity.getValue());
            businessUnits.add(unit);
        }
        return businessUnits;
    }

    private void saveUnits(Collection<BusinessUnit> units) {
        try {
            if(!units.isEmpty()) {
                unitsRepo.save(units);
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