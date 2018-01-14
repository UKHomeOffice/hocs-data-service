package uk.gov.digital.ho.hocs.lists.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.house.model.Member;
import uk.gov.digital.ho.hocs.lists.model.DataListEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class DataListEntityRecord implements Serializable {

    @Getter
    private String text;

    @Getter
    private String value;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<DataListEntityRecordProperty> properties = new ArrayList<>();


    public static DataListEntityRecord create(DataListEntity dle) {
        return new DataListEntityRecord(dle.getText(), dle.getValue(), new ArrayList<>());
    }

    @Deprecated()
    public static DataListEntityRecord create(Member dle, String houseName) {

        List<DataListEntityRecordProperty> properties = new ArrayList<>();
        properties.add(new DataListEntityRecordProperty("house", houseName));

        return new DataListEntityRecord(dle.getDisplayName(), dle.getReferenceName(), properties);
    }

    // This can be removed once we remove the properties field. (see above)
    public DataListEntityRecord(String text, String value){
        this.text = text;
        this.value = value;
    }

    public Map<String, String> getProperties() {
        Map<String, String> propMap = new HashMap<>();
        for (DataListEntityRecordProperty property: this.properties) {
            propMap.put(property.getKey(), property.getValue());
        }
        return propMap;
    }
}
