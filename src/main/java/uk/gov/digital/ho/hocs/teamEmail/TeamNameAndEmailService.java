package uk.gov.digital.ho.hocs.teamEmail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.teamEmail.ingest.CSVTeamNameAndEmail;
import uk.gov.digital.ho.hocs.teamEmail.model.TeamNameAndEmail;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class TeamNameAndEmailService {

    private final TeamNameAndEmailRepository teamNameAndEmailRepository;

    @Autowired
    public TeamNameAndEmailService(TeamNameAndEmailRepository teamNameAndEmailRepository) {
        this.teamNameAndEmailRepository = teamNameAndEmailRepository;
    }

    public TeamNameAndEmail getEmailForTeam(String name) throws EntityNotFoundException {
        TeamNameAndEmail teamNameAndEmail = teamNameAndEmailRepository.findEmailByName(name);

        if (teamNameAndEmail == null) {
            throw new EntityNotFoundException();
        }

        return teamNameAndEmail;

    }

    public void updateTeamEmail(Set<CSVTeamNameAndEmail> csvTeamNameAndEmails) {
        if (csvTeamNameAndEmails != null) {
            teamNameAndEmailRepository.deleteAll();
            Set<TeamNameAndEmail> teamNameAndEmails = new HashSet<>();

            csvTeamNameAndEmails.forEach(cvsTeamEmail -> {
                TeamNameAndEmail teamNameAndEmail = new TeamNameAndEmail();
                teamNameAndEmail.setDisplayName(cvsTeamEmail.getDisplayName());
                teamNameAndEmail.setName(cvsTeamEmail.getName());
                teamNameAndEmail.setEmail(cvsTeamEmail.getEmail());
                teamNameAndEmails.add(teamNameAndEmail);
            });


            teamNameAndEmailRepository.saveAll(teamNameAndEmails);

        }
    }
}
