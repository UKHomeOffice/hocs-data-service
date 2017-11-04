package uk.gov.digital.ho.hocs.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.DataList;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class DataListRecord {
    private String name;
    private List<DataListEntityRecord> entities;

    public static DataListRecord create(DataList list) {
        List<DataListEntityRecord> entities = list.getEntities().stream().map(DataListEntityRecord::create).collect(Collectors.toList());
        return new DataListRecord(list.getName(), entities);
    }
}
