package uk.gov.digital.ho.hocs.businessGroups;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.businessGroups.model.BusinessUnit;

import java.util.Set;

@Repository
public interface BusinessUnitRepository extends CrudRepository<BusinessUnit, Long> {
    Set<BusinessUnit> findAll();
    Set<BusinessUnit> findAllByDeletedIsFalse();

    BusinessUnit findOneByReferenceNameAndDeletedIsFalse(String referenceName);
}