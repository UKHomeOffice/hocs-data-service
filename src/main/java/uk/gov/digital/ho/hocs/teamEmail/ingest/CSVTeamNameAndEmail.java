package uk.gov.digital.ho.hocs.teamEmail.ingest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Getter
@Slf4j
public class CSVTeamNameAndEmail {

    @Getter
    private String displayName;

    @Getter
    private String name;

    @Getter
    private String email;
}
