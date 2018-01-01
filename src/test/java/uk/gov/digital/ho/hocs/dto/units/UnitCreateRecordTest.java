package uk.gov.digital.ho.hocs.dto.units;

import org.junit.Test;
import uk.gov.digital.ho.hocs.model.BusinessGroup;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class UnitCreateRecordTest {

    @Test
    public void createWithEntities() throws Exception {
        Set<BusinessGroup> unitList = new HashSet<>();
        unitList.add(new BusinessGroup());
        UnitCreateRecord record = UnitCreateRecord.create(unitList);
        assertThat(record.getManageGroups()).hasSize(1);
    }

    @Test
    public void createWithoutEntities() throws Exception {
        Set<BusinessGroup> unitList = new HashSet<>();
        UnitCreateRecord record = UnitCreateRecord.create(unitList);
        assertThat(record.getManageGroups()).hasSize(0);
    }

}