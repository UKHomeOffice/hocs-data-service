package uk.gov.digital.ho.hocs;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.model.BusinessTeam;

@Repository
public interface BusinessTeamRepository extends CrudRepository<BusinessTeam, Long> {

    BusinessTeam findOneByReferenceNameAndDeletedIsFalse(String referenceName);
}