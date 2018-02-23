package uk.gov.digital.ho.hocs.lists.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
class DataListEntityRecordProperty implements Serializable {

    @Getter
    private String key;

    @Getter
    private String value;
}