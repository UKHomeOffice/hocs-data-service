package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.model.House;
import uk.gov.digital.ho.hocs.model.Member;

import java.util.Set;

@Service
@Slf4j
public class MemberService {

    private final MemberRepository repo;

    @Autowired
    public MemberService(MemberRepository repo) { this.repo = repo; }

    @Cacheable(value = "members", key = "#houseName")
    public House getHouseByName(String houseName) throws ListNotFoundException {
       House house = repo.findOneByNameAndDeletedIsFalse(houseName);
        if (house == null) {
            throw new ListNotFoundException();
        }
        return house;
    }

    @Cacheable(value = "members", key = "all")
    public Set<House> getAllHouses() throws ListNotFoundException {
        Set<House> list = repo.findAllByDeletedIsFalse();
        if (list.isEmpty()) {
            throw new ListNotFoundException();
        }
        return list;
    }

    @Caching( evict = {@CacheEvict(value = "members", key = "#house.name"),
                       @CacheEvict(value = "members", key = "all")})
    public void updateHouse(House newHouse) {
        if(newHouse != null) {
            House jpaHouse = repo.findOneByName(newHouse.getName());

            // Update existing house
            if (jpaHouse != null) {
                Set<Member> newMembers = newHouse.getMembers();
                Set<Member> jpaMembers = jpaHouse.getMembers();

                // Update existing topic items
                jpaMembers.forEach(item -> {
                    item.setDeleted(!newMembers.contains(item));
                });

                // Add new topic items
                newMembers.forEach(newTopic -> {
                    if (!jpaMembers.contains(newTopic)) {
                        jpaMembers.add(newTopic);
                    }
                });

                jpaHouse.setMembers(jpaMembers);

                // Set the topic group to deleted if there are no visible topic items
                jpaHouse.setDeleted(jpaHouse.getMembers().stream().allMatch(topic -> topic.getDeleted()));
            } else {
                jpaHouse = newHouse;
            }

            saveMembers(jpaHouse);
        } else{
            throw new EntityCreationException("Unable to update entity");
        }
    }

    @CacheEvict(value = "members", allEntries = true)
    public void clearCache(){
        log.info("All members cache cleared");
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