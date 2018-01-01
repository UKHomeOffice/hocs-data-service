package uk.gov.digital.ho.hocs.dto.dataList;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
public class DataListEntityRecordProperty implements Serializable {

    @Getter
    private String key;

    @Getter
    private String value;
}