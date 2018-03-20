package uk.gov.digital.ho.hocs.teamEmail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.digital.ho.hocs.businessGroups.model.BusinessTeam;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.teamEmail.ingest.CSVTeamEmail;
import uk.gov.digital.ho.hocs.teamEmail.model.TeamEmail;
import uk.gov.digital.ho.hocs.user.ingest.CSVUserLine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TeamEmailServiceTest {

    @Mock
    private TeamEmailRepository teamEmailRepository;

    private TeamEmailService teamEmailService;

    @Before
    public void setup(){ teamEmailService = new TeamEmailService(teamEmailRepository);}

    @Test
    public void shouldReturnEmailAddressWhenEntryExists() throws EntityNotFoundException {
        TeamEmail email = new TeamEmail();
        email.setName("name");
        email.setEmail("A@A.COM");
        when(teamEmailRepository.findEmailByName("name")).thenReturn(email);

        String emailAddress = teamEmailService.getEmailForTeam("name");

        assertThat(emailAddress).isEqualTo("A@A.COM");

    }

    @Test(expected = EntityNotFoundException.class)
    public void testEmailNotFoundThrowsEntityNotFoundException() throws EntityNotFoundException {

        String emailAddress = teamEmailService.getEmailForTeam("name");
        verify(teamEmailRepository).findEmailByName("name");
        assertThat(emailAddress).isEmpty();
    }

    @Test
    public void testServiceTeamEmailFromCSV() throws EntityCreationException {
        final Set<CSVTeamEmail> csvTeamEmailSet = generateTeamEmails(10);

        teamEmailService.updateTeamEmail(csvTeamEmailSet);

        verify(teamEmailRepository, times(1)).deleteAll();
        verify(teamEmailRepository).saveAll(any());
    }

    private Set<CSVTeamEmail> generateTeamEmails(int quantity) {
        Set<CSVTeamEmail> teamEmailSet = new HashSet<>();
        for (int i = 0; i < quantity; i++) {
            teamEmailSet.add(new CSVTeamEmail("Test" + i, "Test.User"+i+"@test.com"));
        }
        return teamEmailSet;
    }


}