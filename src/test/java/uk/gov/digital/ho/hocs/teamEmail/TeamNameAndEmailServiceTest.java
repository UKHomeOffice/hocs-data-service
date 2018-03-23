package uk.gov.digital.ho.hocs.teamEmail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.teamEmail.ingest.CSVTeamNameAndEmail;
import uk.gov.digital.ho.hocs.teamEmail.model.TeamNameAndEmail;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TeamNameAndEmailServiceTest {

    @Mock
    private TeamNameAndEmailRepository teamNameAndEmailRepository;

    private TeamNameAndEmailService teamNameAndEmailService;

    @Before
    public void setup(){ teamNameAndEmailService = new TeamNameAndEmailService(teamNameAndEmailRepository);}

    @Test
    public void shouldReturnEmailAddressWhenEntryExists() throws EntityNotFoundException {
        TeamNameAndEmail email = new TeamNameAndEmail();
        email.setDisplayName("Display name");
        email.setName("name");
        email.setEmail("A@A.COM");
        when(teamNameAndEmailRepository.findEmailByName("name")).thenReturn(email);

        TeamNameAndEmail emailAddress = teamNameAndEmailService.getEmailForTeam("name");

        assertThat(emailAddress.getEmail()).isEqualTo("A@A.COM");
        assertThat(emailAddress.getDisplayName()).isEqualTo("Display name");
        assertThat(emailAddress.getName()).isEqualTo("name");

    }

    @Test(expected = EntityNotFoundException.class)
    public void testEmailNotFoundThrowsEntityNotFoundException() throws EntityNotFoundException {

        TeamNameAndEmail emailAddress = teamNameAndEmailService.getEmailForTeam("name");
        verify(teamNameAndEmailRepository).findEmailByName("name");
        assertThat(emailAddress.getEmail()).isEmpty();
    }

    @Test
    public void testServiceTeamEmailFromCSV() throws EntityCreationException {
        final Set<CSVTeamNameAndEmail> csvTeamNameAndEmailSet = generateTeamEmails(10);

        teamNameAndEmailService.updateTeamEmail(csvTeamNameAndEmailSet);

        verify(teamNameAndEmailRepository, times(1)).deleteAll();
        verify(teamNameAndEmailRepository).saveAll(any());
    }

    private Set<CSVTeamNameAndEmail> generateTeamEmails(int quantity) {
        Set<CSVTeamNameAndEmail> teamEmailSet = new HashSet<>();
        for (int i = 0; i < quantity; i++) {
            teamEmailSet.add(new CSVTeamNameAndEmail("TestName" + i,"Test" + i, "Test.User"+i+"@test.com"));
        }
        return teamEmailSet;
    }


}