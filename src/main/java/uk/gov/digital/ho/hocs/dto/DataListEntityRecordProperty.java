package uk.gov.digital.ho.hocs.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.DataListEntityProperty;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class DataListEntityRecordProperty implements Serializable {
    private String key;
    private String value;

    public static DataListEntityRecordProperty create(DataListEntityProperty p) {
        return new DataListEntityRecordProperty(p.getKey(), p.getValue());
    }
}