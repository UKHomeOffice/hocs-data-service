package uk.gov.digital.ho.hocs.businessGroups;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.businessGroups.model.BusinessTeam;

@Repository
public interface BusinessTeamRepository extends CrudRepository<BusinessTeam, Long> {

    BusinessTeam findOneByReferenceNameAndDeletedIsFalse(String referenceName);
}