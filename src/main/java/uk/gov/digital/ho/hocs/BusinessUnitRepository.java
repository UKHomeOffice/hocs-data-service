package uk.gov.digital.ho.hocs;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.model.BusinessUnit;

import java.util.Set;

@Repository
public interface BusinessUnitRepository extends CrudRepository<BusinessUnit, Long> {
    Set<BusinessUnit> findAll();
    Set<BusinessUnit> findAllByDeletedIsFalse();
}