package uk.gov.digital.ho.hocs.teamEmail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.teamEmail.model.TeamNameAndEmail;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest
public class TeamNameAndEmailRepositoryTest {

    @Autowired
    private TeamNameAndEmailRepository teamNameAndEmailRepository;

    @Before
    public void setup() throws EntityCreationException {
        teamNameAndEmailRepository.deleteAll();

        TeamNameAndEmail teamNameAndEmail = new TeamNameAndEmail();
        teamNameAndEmail.setDisplayName("disp");
        teamNameAndEmail.setName("A");
        teamNameAndEmail.setEmail("a@a.com");
        teamNameAndEmailRepository.save(teamNameAndEmail);
    }

    @Test
    public void shouldRetrieveEmailAddressForRequestedTeam(){
        TeamNameAndEmail retrievedTeamNameAndEmail = teamNameAndEmailRepository.findEmailByName("A");

        assertThat(retrievedTeamNameAndEmail.getDisplayName()).isEqualTo("disp");
        assertThat(retrievedTeamNameAndEmail.getEmail()).isEqualTo("a@a.com");
    }

}