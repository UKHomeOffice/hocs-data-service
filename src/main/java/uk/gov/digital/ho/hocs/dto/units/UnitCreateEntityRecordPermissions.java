package uk.gov.digital.ho.hocs.dto.units;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class UnitCreateEntityRecordPermissions implements Serializable {

    private String groupName;
    private String groupPermission;


}