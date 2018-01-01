package uk.gov.digital.ho.hocs.dto.units;

import org.junit.Test;
import uk.gov.digital.ho.hocs.exception.GroupCreationException;
import uk.gov.digital.ho.hocs.model.BusinessTeam;
import uk.gov.digital.ho.hocs.model.BusinessUnit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class BusinessUnitRecordTest {
    @Test
    public void createUnit() throws Exception, GroupCreationException {

        BusinessUnit unit = new BusinessUnit("Disp", "Auth");
        Set<BusinessTeam> teams = new HashSet<>();
        teams.add(new BusinessTeam("display"));
        unit.setTeams(teams);
        BusinessUnitRecord unitRecord = BusinessUnitRecord.create(unit);

        assertThat(unitRecord.getAuthorityName()).isEqualTo("GROUP_AUTH");
        assertThat(unitRecord.getDisplayName()).isEqualTo("Disp");
        assertThat(unitRecord.getTeams()).hasSize(1);
    }

    @Test
    public void getStrings() throws Exception {
        List<BusinessTeamRecord> units = new ArrayList<>();
        units.add(new BusinessTeamRecord("", ""));
        BusinessUnitRecord unit = new BusinessUnitRecord("Auth", "Disp", units);

        assertThat(unit.getAuthorityName()).isEqualTo("Auth");
        assertThat(unit.getDisplayName()).isEqualTo("Disp");
        assertThat(unit.getTeams()).hasSize(1);
    }

}