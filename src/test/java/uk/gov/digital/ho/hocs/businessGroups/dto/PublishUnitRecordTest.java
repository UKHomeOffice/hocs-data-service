package uk.gov.digital.ho.hocs.businessGroups.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.businessGroups.model.BusinessUnit;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PublishUnitRecordTest {

    @Test
    public void createWithEntities() throws EntityCreationException {
        Set<BusinessUnit> unitList = new HashSet<>();
        unitList.add(new BusinessUnit("disp"));
        PublishUnitRecord record = PublishUnitRecord.create(unitList);
        assertThat(record.getManageGroups()).hasSize(1);
    }

    @Test
    public void createWithoutEntities() {
        Set<BusinessUnit> unitList = new HashSet<>();
        PublishUnitRecord record = PublishUnitRecord.create(unitList);
        assertThat(record.getManageGroups()).hasSize(0);
    }

}