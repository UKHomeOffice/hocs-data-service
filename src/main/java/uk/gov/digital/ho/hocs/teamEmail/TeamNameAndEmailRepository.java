package uk.gov.digital.ho.hocs.teamEmail;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.teamEmail.model.TeamNameAndEmail;

@Repository
public interface TeamNameAndEmailRepository extends CrudRepository<TeamNameAndEmail, String> {

    TeamNameAndEmail findEmailByName(String name);
}
