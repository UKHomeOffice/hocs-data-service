package uk.gov.digital.ho.hocs.teamEmail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import uk.gov.digital.ho.hocs.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.teamEmail.dto.TeamNameAndEmailDto;
import uk.gov.digital.ho.hocs.teamEmail.model.TeamNameAndEmail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TeamNameAndEmailResourceTest {


    private TeamNameAndEmailResource teamNameAndEmailResource;

    @Mock
    private TeamNameAndEmailService teamNameAndEmailService;

    @Before
    public void setUp() {
        teamNameAndEmailResource = new TeamNameAndEmailResource(teamNameAndEmailService);
    }

    @Test
    public void shouldRetrieveEmailForRequestedTeam() throws EntityNotFoundException {
        TeamNameAndEmail response = new TeamNameAndEmail();
        response.setEmail("theAteam@example.com");
        response.setDisplayName("ATeam");
        when(teamNameAndEmailService.getEmailForTeam("The A-Team")).thenReturn(response);

        ResponseEntity<TeamNameAndEmailDto> httpResponse = (teamNameAndEmailResource.getEmailForTeam("The A-Team"));

        assertThat(httpResponse.getBody().getEmail()).isEqualTo("theAteam@example.com");
        assertThat(httpResponse.getBody().getDisplayName()).isEqualTo("ATeam");
    }

    @Test
    public void shouldReturnNotFoundWhenException() throws EntityNotFoundException {
        when(teamNameAndEmailService.getEmailForTeam("FAKE_TEAM")).thenThrow(new EntityNotFoundException());

        ResponseEntity<TeamNameAndEmailDto> httpResponse = (teamNameAndEmailResource.getEmailForTeam("FAKE_TEAM"));

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(httpResponse.getBody()).isNull();

    }

    @Test
    public void ShouldUpdateTeamEmail() throws IOException {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("DisplayName,TeamName,TeamNameAndEmail\n");
        csvBuilder.append("A,A,A@A.com\n");
        csvBuilder.append("A,B,B@B.com\n");
        InputStream is = new ByteArrayInputStream(csvBuilder.toString().getBytes());

        MockMultipartFile file = new MockMultipartFile("file", "orig", MediaType.TEXT_PLAIN_VALUE, is);
        ResponseEntity httpResponse = teamNameAndEmailResource.updateTeamEmail(file);
        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(teamNameAndEmailService, times(1)).updateTeamEmail(any());
    }


}