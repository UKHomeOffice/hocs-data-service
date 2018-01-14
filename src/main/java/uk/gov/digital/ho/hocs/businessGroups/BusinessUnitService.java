package uk.gov.digital.ho.hocs.businessGroups;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.AlfrescoClient;
import uk.gov.digital.ho.hocs.businessGroups.ingest.CSVBusinessGroupLine;
import uk.gov.digital.ho.hocs.businessGroups.model.BusinessTeam;
import uk.gov.digital.ho.hocs.businessGroups.model.BusinessUnit;
import uk.gov.digital.ho.hocs.exception.AlfrescoPostException;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.EntityNotFoundException;

import java.util.*;

@Service
@Slf4j
public class BusinessUnitService {
    private final BusinessUnitRepository unitsRepo;
    private final BusinessTeamRepository teamsRepo;
    private final AlfrescoClient alfrescoClient;

    @Autowired
    public BusinessUnitService(BusinessUnitRepository unitsRepo, BusinessTeamRepository teamsRepo,  AlfrescoClient alfrescoClient) {
        this.unitsRepo = unitsRepo;
        this.teamsRepo = teamsRepo;
        this.alfrescoClient = alfrescoClient;
    }

    void publishGroups() throws EntityNotFoundException, AlfrescoPostException {
        Set<BusinessUnit> list = unitsRepo.findAll();
        if(list == null) {
            throw new EntityNotFoundException();
        }
        alfrescoClient.postUnits(list);
    }

    @Cacheable(value = "units", key="#referenceName")
    public Set<BusinessTeam> getTeamByReference(String referenceName) throws EntityNotFoundException {
        Set<BusinessTeam> teams = new HashSet<>();

        BusinessTeam businessTeam = teamsRepo.findOneByReferenceNameAndDeletedIsFalse(referenceName);

        if(businessTeam != null){
            teams.add(businessTeam);
        }
        else{
            // Might be trying to add permissions to an entire unit
            BusinessUnit businessUnit = unitsRepo.findOneByReferenceNameAndDeletedIsFalse(referenceName);
            if(businessUnit != null && businessUnit.getTeams() != null){
                teams.addAll(businessUnit.getTeams());
            }
            else {
                throw new EntityNotFoundException();
            }

        }
        return teams;
    }

    @Cacheable(value = "units")
    public Set<BusinessUnit> getAllBusinessUnits() {
        return unitsRepo.findAllByDeletedIsFalse();
    }

    @CacheEvict(value = "units", allEntries = true)
    public void updateBusinessUnits(Set<CSVBusinessGroupLine> lines) throws EntityCreationException {
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
                    jpaUnit.setDeleted(jpaUnit.getTeams().stream().allMatch(BusinessTeam::getDeleted));

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

    private static List<BusinessUnit> buildBusinessUnits(Set<CSVBusinessGroupLine> lines) throws EntityCreationException {
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