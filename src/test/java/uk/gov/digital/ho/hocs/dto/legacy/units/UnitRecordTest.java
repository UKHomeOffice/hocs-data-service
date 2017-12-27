package uk.gov.digital.ho.hocs.dto.legacy.units;

import org.junit.Test;
import uk.gov.digital.ho.hocs.model.BusinessGroup;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class UnitRecordTest {

    @Test
    public void createWithEntities() throws Exception {
        Set<BusinessGroup> unitList = new HashSet<>();
        unitList.add(new BusinessGroup());
        UnitRecord record = UnitRecord.create(unitList);
        assertThat(record.getUnits()).hasSize(1);
    }

    @Test
    public void createWithoutEntities() throws Exception {
        Set<BusinessGroup> unitList = new HashSet<>();
        UnitRecord record = UnitRecord.create(unitList);
        assertThat(record.getUnits()).hasSize(0);
    }

}