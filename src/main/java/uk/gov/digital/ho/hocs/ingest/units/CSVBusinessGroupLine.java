package uk.gov.digital.ho.hocs.ingest.units;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Getter
@Slf4j
@EqualsAndHashCode(of = {"unitReference", "teamReference"})
public class CSVBusinessGroupLine {

    @Getter
    private String unitDisplay;

    @Getter
    private String unitReference;

    @Getter
    private String teamDisplay;

    @Getter
    private String teamReference;

    public String getTeamValue() {
        return String.format("%s_%s", unitReference, teamReference);
    }

}