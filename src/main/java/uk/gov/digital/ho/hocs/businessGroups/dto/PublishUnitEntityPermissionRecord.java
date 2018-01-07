package uk.gov.digital.ho.hocs.businessGroups.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
public class PublishUnitEntityPermissionRecord implements Serializable {

    @Getter
    private String groupName;

    @Getter
    private String groupPermission;

}