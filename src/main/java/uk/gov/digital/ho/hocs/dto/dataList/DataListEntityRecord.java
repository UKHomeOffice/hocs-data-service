package uk.gov.digital.ho.hocs.dto.dataList;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.DataListEntity;
import uk.gov.digital.ho.hocs.model.Member;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class DataListEntityRecord implements Serializable {

    @Getter
    private String text;

    @Getter
    private String value;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
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
}
