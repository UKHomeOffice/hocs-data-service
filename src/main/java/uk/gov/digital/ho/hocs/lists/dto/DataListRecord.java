package uk.gov.digital.ho.hocs.lists.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.lists.model.DataList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
public class DataListRecord implements Serializable {

    @Getter
    private String name;

    @Getter
    private List<DataListEntityRecord> entities = new ArrayList<>();

    public static DataListRecord create(DataList list) {
        return create(list, false);
    }

    public static DataListRecord create(DataList list, boolean showDeleted) {
        List<DataListEntityRecord> entities = list.getEntities().stream().filter(topic -> !topic.getDeleted() || showDeleted).map(DataListEntityRecord::create).collect(Collectors.toList());
        return new DataListRecord(list.getName(), entities);
    }
}