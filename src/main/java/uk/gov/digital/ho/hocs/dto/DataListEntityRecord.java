package uk.gov.digital.ho.hocs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.DataListEntity;
import uk.gov.digital.ho.hocs.model.Member;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public class DataListEntityRecord implements Serializable {

    private String text;

    private String value;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<DataListEntityRecordProperty> properties = new ArrayList<>();


    public static DataListEntityRecord create(DataListEntity dle) {
        return new DataListEntityRecord(dle.getText(), dle.getValue(), new ArrayList<>());
    }

    public static DataListEntityRecord create(Member dle, String houseName) {

        List<DataListEntityRecordProperty> properties = new ArrayList<>();
        properties.add(new DataListEntityRecordProperty("HOUSE", houseName));

        return new DataListEntityRecord(dle.getDisplayName(), dle.getReferenceName(), properties);
    }

    public DataListEntityRecord(String text) {
        this(text, text);
    }

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
