package uk.gov.digital.ho.hocs;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.model.House;

import java.util.Set;

@Repository
public interface MemberRepository extends CrudRepository<House, Long> {

    House findOneByNameAndDeletedIsFalse(String name);

    Set<House> findAllByDeletedIsFalse();
}