package uk.gov.digital.ho.hocs.house;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.exception.IngestException;
import uk.gov.digital.ho.hocs.house.ingest.ListConsumerService;
import uk.gov.digital.ho.hocs.house.model.House;
import uk.gov.digital.ho.hocs.house.model.Member;

import java.util.Set;

@Service
@Slf4j
public class HouseService {

    private final HouseRepository repo;

    private final ListConsumerService listConsumerService;

    @Autowired
    public HouseService(HouseRepository repo, ListConsumerService listConsumerService) {
        this.repo = repo;
        this.listConsumerService = listConsumerService;
    }

    @Cacheable(value = "members", key = "#houseName")
    public House getHouseByName(String houseName) throws EntityNotFoundException {
       House house = repo.findOneByNameAndDeletedIsFalse(houseName);
        if (house == null) {
            throw new EntityNotFoundException();
        }
        return house;
    }

    @Cacheable(value = "members")
    public Set<House> getAllHouses() {
        return repo.findAllByDeletedIsFalse();
    }

    @CacheEvict(value = "members", allEntries = true)
    public void updateHouse(House newHouse) {
        if(newHouse != null) {
            House jpaHouse = repo.findOneByName(newHouse.getName());

            // Update existing house
            if (jpaHouse != null) {
                Set<Member> newMembers = newHouse.getMembers();
                Set<Member> jpaMembers = jpaHouse.getMembers();

                // Update existing members
                jpaMembers.forEach(item -> {
                    item.setDeleted(!newMembers.contains(item));
                });

                // Add new members
                newMembers.forEach(newMember -> {
                    if (!jpaMembers.contains(newMember)) {
                        jpaMembers.add(newMember);
                    }
                });

                jpaHouse.setMembers(jpaMembers);

                // Set the house to deleted if there are no visible members
                jpaHouse.setDeleted(jpaHouse.getMembers().stream().allMatch(Member::getDeleted));
            } else {
                jpaHouse = newHouse;
            }

            saveMembers(jpaHouse);
        } else{
            throw new EntityCreationException("Unable to update entity");
        }
    }

    @CacheEvict(value = "members", allEntries = true)
    public void updateWebMemberLists() throws IngestException {
        updateHouse(listConsumerService.createFromEuropeanParliamentAPI());
        updateHouse(listConsumerService.createFromIrishAssemblyAPI());
        updateHouse(listConsumerService.createFromScottishParliamentAPI());
        updateHouse(listConsumerService.createCommonsFromUKParliamentAPI());
        updateHouse(listConsumerService.createLordsFromUKParliamentAPI());
        updateHouse(listConsumerService.createFromWelshAssemblyAPI());
    }

    private void saveMembers(House house) {
        try {
            if(house != null && house.getName() != null) {
                repo.save(house);
            }
        } catch (DataIntegrityViolationException e) {

            if (e.getCause() instanceof ConstraintViolationException &&
                    ((ConstraintViolationException) e.getCause()).getConstraintName().toLowerCase().contains("house_name_idempotent") ||
                    ((ConstraintViolationException) e.getCause()).getConstraintName().toLowerCase().contains("member_name_ref_idempotent")) {
                throw new EntityCreationException("Identified an attempt to recreate existing entity, rolling back");
            }

            throw e;
        }

    }

}