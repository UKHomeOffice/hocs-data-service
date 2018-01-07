package uk.gov.digital.ho.hocs.house;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.house.model.House;

import java.util.Set;

@Repository
public interface HouseRepository extends CrudRepository<House, Long> {

    House findOneByName(String name);
    House findOneByNameAndDeletedIsFalse(String name);

    Set<House> findAllByDeletedIsFalse();
}