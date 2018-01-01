package uk.gov.digital.ho.hocs.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.dto.dataList.DataListEntityRecord;
import uk.gov.digital.ho.hocs.dto.dataList.DataListRecord;
import uk.gov.digital.ho.hocs.model.DataList;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DataListRecordTest {

    @Test
    public void createWithNoEntities() throws Exception {
        List<DataListEntityRecord> entities = new ArrayList<>();
        DataList datalist = new DataList(new DataListRecord("TEST List", entities));
        DataListRecord record = DataListRecord.create(datalist);

        assertThat(record.getName()).isEqualTo(datalist.getName());
        assertThat(record.getEntities()).isEmpty();
    }

    @Test
    public void createWithEntities() throws Exception {
        List<DataListEntityRecord> entities = new ArrayList<>();
        entities.add(new DataListEntityRecord("entity1", "entity1"));

        DataList datalist = new DataList(new DataListRecord("TEST List", entities));
        DataListRecord record = DataListRecord.create(datalist);

        assertThat(record.getName()).isEqualTo(datalist.getName());
        assertThat(record.getEntities()).hasSize(1);
    }
}