package uk.gov.digital.ho.hocs.teamEmail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.teamEmail.dto.TeamNameAndEmailDto;
import uk.gov.digital.ho.hocs.teamEmail.ingest.CSVTeamNameAndEmail;
import uk.gov.digital.ho.hocs.teamEmail.ingest.TeamNameAndEmailParser;
import uk.gov.digital.ho.hocs.teamEmail.model.TeamNameAndEmail;

import java.util.Set;

@Slf4j
@RestController
public class TeamNameAndEmailResource {

    private final TeamNameAndEmailService teamNameAndEmailService;

    @Autowired
    public TeamNameAndEmailResource(TeamNameAndEmailService teamNameAndEmailService) {
        this.teamNameAndEmailService = teamNameAndEmailService;
    }

    @RequestMapping(value = "/team/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<TeamNameAndEmailDto> getEmailForTeam(@PathVariable("name") String name) {
        log.info("Email address for team \"{}\" requested", name);
        try {
            TeamNameAndEmail teamNameAndEmail = teamNameAndEmailService.getEmailForTeam(name);

            return ResponseEntity.ok(TeamNameAndEmailDto.builder().email(teamNameAndEmail.getEmail()).displayName(teamNameAndEmail.getDisplayName()).build());
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
                Set<CSVTeamNameAndEmail> entries = new TeamNameAndEmailParser(file).getLines();
                teamNameAndEmailService.updateTeamEmail(entries);
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
