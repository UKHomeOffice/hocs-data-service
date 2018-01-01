package uk.gov.digital.ho.hocs.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class DataListEntityRecordProperty implements Serializable {
    private String key;
    private String value;
}