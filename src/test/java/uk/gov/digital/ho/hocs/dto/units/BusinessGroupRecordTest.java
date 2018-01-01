package uk.gov.digital.ho.hocs.dto.units;

import org.junit.Test;
import uk.gov.digital.ho.hocs.exception.GroupCreationException;
import uk.gov.digital.ho.hocs.model.BusinessGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class BusinessGroupRecordTest {
    @Test
    public void createUnit() throws Exception, GroupCreationException {

        BusinessGroup group = new BusinessGroup("Disp", "Auth");
        Set<BusinessGroup> units = new HashSet<>();
        units.add(new BusinessGroup("display"));
        group.setSubGroups(units);
        BusinessGroupRecord unit = BusinessGroupRecord.create(group);

        assertThat(unit.getAuthorityName()).isEqualTo("GROUP_AUTH");
        assertThat(unit.getDisplayName()).isEqualTo("Disp");
        assertThat(unit.getTeams()).hasSize(1);
    }

    @Test
    public void getStrings() throws Exception {
        List<BusinessGroupRecord> units = new ArrayList<>();
        units.add(new BusinessGroupRecord("", "", new ArrayList<>()));
        BusinessGroupRecord unit = new BusinessGroupRecord("Auth", "Disp", units);

        assertThat(unit.getAuthorityName()).isEqualTo("Auth");
        assertThat(unit.getDisplayName()).isEqualTo("Disp");
        assertThat(unit.getTeams()).hasSize(1);
    }

}