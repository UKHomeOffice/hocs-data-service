package uk.gov.digital.ho.hocs.dto.legacy.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import uk.gov.digital.ho.hocs.dto.legacy.units.UnitCreateEntityRecord;
import uk.gov.digital.ho.hocs.dto.legacy.units.UnitCreateEntityRecordPermissionGroup;
import uk.gov.digital.ho.hocs.dto.legacy.units.UnitCreateEntityRecordPermissions;
import uk.gov.digital.ho.hocs.model.User;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
@Log4j
public class UserCreateRecord implements Serializable {
    private Set<UserCreateEntityRecord> users;
    private Set<UnitCreateEntityRecord> units;
    private List<UnitCreateEntityRecordPermissionGroup> permissions;

    public static UserCreateRecord create(Set<User> list) {
        Set<UserCreateEntityRecord> users = list.stream()
                .map(UserCreateEntityRecord::create)
                .collect(Collectors.toSet());

        List<String> permissionGroups = users.stream()
                .map(UserCreateEntityRecord::getGroupNameArray)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        List<UnitCreateEntityRecordPermissions> permissions = permissionGroups.stream()
                .map(i -> new UnitCreateEntityRecordPermissions(i, "Drafter"))
                .collect(Collectors.toList());

        List<UnitCreateEntityRecordPermissionGroup> permissionGroupList = new ArrayList<>();
        permissionGroupList.add(new UnitCreateEntityRecordPermissionGroup("Cases", permissions));
        permissionGroupList.add(new UnitCreateEntityRecordPermissionGroup("Auto Create", permissions));

        return new UserCreateRecord(users, new HashSet<>(), permissionGroupList);
    }
}
