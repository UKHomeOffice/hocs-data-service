package uk.gov.digital.ho.hocs.dto.legacy.units;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Getter
public class UnitCreateEntityRecordPermissionGroup implements Serializable {

    private String folderName;

    private List<UnitCreateEntityRecordPermissions> permissions;


}