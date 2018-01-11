package uk.gov.digital.ho.hocs.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import uk.gov.digital.ho.hocs.businessGroups.dto.PublishUnitEntityPermissionListRecord;
import uk.gov.digital.ho.hocs.businessGroups.dto.PublishUnitEntityPermissionRecord;
import uk.gov.digital.ho.hocs.businessGroups.dto.PublishUnitEntityRecord;
import uk.gov.digital.ho.hocs.user.model.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Log4j
public class PublishUserListRecord implements Serializable {

    @Getter
    private Set<PublishUserRecord> users;

    @Getter
    private Set<PublishUnitEntityRecord> units;

    @Getter
    private List<PublishUnitEntityPermissionListRecord> permissions;

    public static PublishUserListRecord create(Set<User> list) {
        Set<PublishUserRecord> users = list.stream()
                .map(PublishUserRecord::create)
                .collect(Collectors.toSet());

        List<String> permissionGroups = users.stream()
                .map(PublishUserRecord::getGroupNameArray)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        List<PublishUnitEntityPermissionRecord> permissions = permissionGroups.stream()
                .map(i -> new PublishUnitEntityPermissionRecord(i, "Drafter"))
                .collect(Collectors.toList());

        List<PublishUnitEntityPermissionListRecord> permissionGroupList = new ArrayList<>();
        permissionGroupList.add(new PublishUnitEntityPermissionListRecord("Cases", permissions));
        permissionGroupList.add(new PublishUnitEntityPermissionListRecord("Auto Create", permissions));
        permissionGroupList.add(new PublishUnitEntityPermissionListRecord("Standard Lines", permissions));
        permissionGroupList.add(new PublishUnitEntityPermissionListRecord("Document Templates", permissions));

        return new PublishUserListRecord(users, new HashSet<>(), permissionGroupList);
    }
}
