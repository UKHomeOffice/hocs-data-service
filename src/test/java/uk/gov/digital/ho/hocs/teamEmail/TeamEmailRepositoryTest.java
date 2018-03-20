package uk.gov.digital.ho.hocs.teamEmail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.teamEmail.model.TeamEmail;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest
public class TeamEmailRepositoryTest {

    @Autowired
    private TeamEmailRepository teamEmailRepository;

    @Before
    public void setup() throws EntityCreationException {
        teamEmailRepository.deleteAll();

        TeamEmail teamEmail = new TeamEmail();
        teamEmail.setName("A");
        teamEmail.setEmail("a@a.com");
        teamEmailRepository.save(teamEmail);
    }

    @Test
    public void shouldRetrieveEmailAddressForRequestedTeam(){
        TeamEmail retrievedTeamEmail = teamEmailRepository.findEmailByName("A");

        assertThat(retrievedTeamEmail.getEmail()).isEqualTo("a@a.com");
    }

}