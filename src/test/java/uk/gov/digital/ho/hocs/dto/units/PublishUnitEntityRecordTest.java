package uk.gov.digital.ho.hocs.dto.units;

import org.junit.Test;
import uk.gov.digital.ho.hocs.exception.GroupCreationException;
import uk.gov.digital.ho.hocs.model.BusinessGroup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PublishUnitEntityRecordTest {
    @Test
    public void createTeam() throws Exception, GroupCreationException {
        BusinessGroup group = new BusinessGroup("TeamDisp", "TeamRef");
        PublishUnitEntityRecord entityRecord = PublishUnitEntityRecord.createTeam(group, "UnitRef");

        assertThat(entityRecord.getAction()).isEqualTo("addTeam");
        assertThat(entityRecord.getTeamDisplayName()).isEqualTo("TeamDisp");
        assertThat(entityRecord.getTeamRefName()).isEqualTo("GROUP_TEAMREF");
        assertThat(entityRecord.getUnitDisplayName()).isEqualTo(null);
        assertThat(entityRecord.getUnitRefName()).isEqualTo("UnitRef");
    }

    @Test
    public void createUnit() throws Exception, GroupCreationException {
        BusinessGroup group = new BusinessGroup("UnitDisp", "UnitRef");
        PublishUnitEntityRecord entityRecord = PublishUnitEntityRecord.createUnit(group);

        assertThat(entityRecord.getAction()).isEqualTo("addUnit");
        assertThat(entityRecord.getTeamDisplayName()).isEqualTo(null);
        assertThat(entityRecord.getTeamRefName()).isEqualTo(null);
        assertThat(entityRecord.getUnitDisplayName()).isEqualTo("UnitDisp");
        assertThat(entityRecord.getUnitRefName()).isEqualTo("GROUP_UNITREF");
    }

    @Test
    public void createGroups() throws Exception, GroupCreationException {
        BusinessGroup group = new BusinessGroup("UnitDisp", "UnitRef");
        BusinessGroup subGroupOne = new BusinessGroup("SubUnitDisp1", "SubUnitRef");
        BusinessGroup subGroupTwo = new BusinessGroup("SubUnitDisp2", "SubUnitRef");
        Set<BusinessGroup> subGroups = new HashSet<>();
        subGroups.add(subGroupOne);
        subGroups.add(subGroupTwo);
        group.setSubGroups(subGroups);
        List<PublishUnitEntityRecord> entityRecords = PublishUnitEntityRecord.createGroups(group);

        assertThat(entityRecords).hasSize(3);
        assertThat(entityRecords.get(0).getAction()).isEqualTo("addUnit");
        assertThat(entityRecords.get(0).getUnitDisplayName()).isEqualTo("UnitDisp");
        assertThat(entityRecords.get(1).getAction()).isEqualTo("addTeam");
        assertThat(entityRecords.get(2).getAction()).isEqualTo("addTeam");
    }

    @Test
    public void getStrings() throws Exception {
        PublishUnitEntityRecord entityRecord = new PublishUnitEntityRecord("addTeam", "UnitDisp", "UnitRef", "TeamDisp", "TeamRef");

        assertThat(entityRecord.getAction()).isEqualTo("addTeam");
        assertThat(entityRecord.getTeamDisplayName()).isEqualTo("TeamDisp");
        assertThat(entityRecord.getTeamRefName()).isEqualTo("TeamRef");
        assertThat(entityRecord.getUnitDisplayName()).isEqualTo("UnitDisp");
        assertThat(entityRecord.getUnitRefName()).isEqualTo("UnitRef");
    }

}