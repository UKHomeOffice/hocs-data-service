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
import org.springframework.web.multipart.MultipartFile;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.lists.dto.DataListRecord;
import uk.gov.digital.ho.hocs.lists.model.DataList;
import uk.gov.digital.ho.hocs.teamEmail.ingest.CSVTeamEmail;
import uk.gov.digital.ho.hocs.teamEmail.model.TeamEmail;
import uk.gov.digital.ho.hocs.topics.dto.TopicGroupRecord;

import javax.validation.constraints.AssertTrue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TeamEmailResourceTest {

    @Mock
    private TeamEmailResource teamEmailResource;

    @Mock
    private TeamEmailService teamEmailService;

    @Before
    public void setUp() {
        teamEmailResource = new TeamEmailResource(teamEmailService);
    }

    @Test
    public void shouldRetrieveEmailForRequestedTeam() throws EntityNotFoundException {
        when(teamEmailService.getEmailForTeam("The A-Team")).thenReturn("theAteam@example.com");

        ResponseEntity<String> httpResponse = (teamEmailResource.getEmailForTeam("The A-Team"));

        assertThat(httpResponse.getBody()).isEqualTo("theAteam@example.com");
    }

    @Test
    public void shouldReturnNotFoundWhenException() throws EntityNotFoundException {
        when(teamEmailService.getEmailForTeam("FAKE_TEAM")).thenThrow(new EntityNotFoundException());

        ResponseEntity<String> httpResponse = (teamEmailResource.getEmailForTeam("FAKE_TEAM"));

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(httpResponse.getBody()).isNull();

    }

    @Test
    public void ShouldUpdateTeamEmail() throws IOException {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("TeamName,TeamEmail\n");
        csvBuilder.append("A,A@A.com\n");
        csvBuilder.append("B,B@B.com\n");
        InputStream is = new ByteArrayInputStream(csvBuilder.toString().getBytes());

        MockMultipartFile file = new MockMultipartFile("file", "orig", MediaType.TEXT_PLAIN_VALUE, is);
        ResponseEntity httpResponse = teamEmailResource.updateTeamEmail(file);
        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(teamEmailService, times(1)).updateTeamEmail(any());
    }


}