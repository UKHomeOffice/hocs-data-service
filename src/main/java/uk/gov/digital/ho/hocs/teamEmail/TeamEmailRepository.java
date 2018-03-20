package uk.gov.digital.ho.hocs.teamEmail;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.teamEmail.model.TeamEmail;

@Repository
public interface TeamEmailRepository extends CrudRepository<TeamEmail, String> {

    TeamEmail findEmailByName(String name);
}
