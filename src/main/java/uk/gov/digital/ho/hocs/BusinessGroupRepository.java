package uk.gov.digital.ho.hocs;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.model.BusinessGroup;

import java.util.Set;

@Repository
public interface BusinessGroupRepository extends CrudRepository<BusinessGroup, Long> {

    BusinessGroup findOneByReferenceNameAndDeletedIsFalse(String referenceName);

    Set<BusinessGroup> findAll();
    Set<BusinessGroup> findAllByDeletedIsFalse();
}