package uk.gov.digital.ho.hocs.teamEmail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.teamEmail.ingest.CSVTeamEmail;
import uk.gov.digital.ho.hocs.teamEmail.model.TeamEmail;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class TeamEmailService {

    private final TeamEmailRepository teamEmailRepository;

    @Autowired
    public TeamEmailService(TeamEmailRepository teamEmailRepository) {
        this.teamEmailRepository = teamEmailRepository;
    }

    public String getEmailForTeam(String name) throws EntityNotFoundException {
        TeamEmail teamEmail = teamEmailRepository.findEmailByName(name);

        if (teamEmail == null) {
            throw new EntityNotFoundException();
        }

        return teamEmail.getEmail();

    }

    public void updateTeamEmail(Set<CSVTeamEmail> csvTeamEmails) {
        if (csvTeamEmails != null) {
            teamEmailRepository.deleteAll();
            Set<TeamEmail> teamEmails = new HashSet<>();

            csvTeamEmails.forEach(cvsTeamEmail -> {
                TeamEmail teamEmail = new TeamEmail();
                teamEmail.setName(cvsTeamEmail.getName());
                teamEmail.setEmail(cvsTeamEmail.getEmail());
                teamEmails.add(teamEmail);
            });


            teamEmailRepository.saveAll(teamEmails);

        }
    }
}
