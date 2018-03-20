package uk.gov.digital.ho.hocs.teamEmail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.teamEmail.ingest.CSVTeamEmail;
import uk.gov.digital.ho.hocs.teamEmail.ingest.TeamEmailParser;
import uk.gov.digital.ho.hocs.topics.ingest.CSVTopicLine;

import java.util.Set;

@Slf4j
@RestController
public class TeamEmailResource {

    private final TeamEmailService teamEmailService;

    @Autowired
    public TeamEmailResource(TeamEmailService teamEmailService) {
        this.teamEmailService = teamEmailService;
    }

    @RequestMapping(value = "/team/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> getEmailForTeam(@PathVariable("name") String name) {
        log.info("Email address for team \"{}\" requested", name);
        try {
            String emailAddress = teamEmailService.getEmailForTeam(name);

            return ResponseEntity.ok(emailAddress);
        } catch (EntityNotFoundException e) {
            log.info("Email address for team  \"{}\" not found", name);
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/teamEmail", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity updateTeamEmail(@RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            log.info("Parsing Teams email addresses");
            try {
                Set<CSVTeamEmail> entries = new TeamEmailParser(file).getLines();
                teamEmailService.updateTeamEmail(entries);
                return ResponseEntity.ok().build();
            } catch (EntityCreationException e) {
                log.info("team email not created");
                log.info(e.getMessage());
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }


}
