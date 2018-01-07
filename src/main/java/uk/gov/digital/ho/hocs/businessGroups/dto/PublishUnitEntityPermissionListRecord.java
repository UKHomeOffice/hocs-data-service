package uk.gov.digital.ho.hocs.businessGroups.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
public class PublishUnitEntityPermissionListRecord implements Serializable {

    @Getter
    private String folderName;

    @Getter
    private List<PublishUnitEntityPermissionRecord> permissions;


}