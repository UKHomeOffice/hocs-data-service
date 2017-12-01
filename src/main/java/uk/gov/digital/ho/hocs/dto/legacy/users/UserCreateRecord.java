package uk.gov.digital.ho.hocs.dto.legacy.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import uk.gov.digital.ho.hocs.dto.legacy.units.UnitCreateEntityRecord;
import uk.gov.digital.ho.hocs.dto.legacy.units.UnitCreateEntityRecordPermissionGroup;
import uk.gov.digital.ho.hocs.dto.legacy.units.UnitCreateEntityRecordPermissions;
import uk.gov.digital.ho.hocs.model.User;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
@Log4j
public class UserCreateRecord {
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

        List<String> truncationWarnings = permissionGroups.stream()
                .filter(i -> i.length() > 100)
                .sorted(Comparator.comparing(String::length).reversed())
                .collect(Collectors.toList());

        if (truncationWarnings.size() > 0)
            log.warn(String.format("%d group names were found that exceed the 100 character limit imposed by Alfresco", truncationWarnings.size()));
            truncationWarnings.forEach(i -> log.warn(String.format("Length: %d | ID: %s)", i.length(), i)));

        List<UnitCreateEntityRecordPermissions> permissions = permissionGroups.stream()
                .map(i -> new UnitCreateEntityRecordPermissions(i, "Drafter"))
                .collect(Collectors.toList());

        List<UnitCreateEntityRecordPermissionGroup> permissionGroupList = new ArrayList<>();
        permissionGroupList.add(new UnitCreateEntityRecordPermissionGroup("Cases", permissions));
        permissionGroupList.add(new UnitCreateEntityRecordPermissionGroup("Auto Create", permissions));

        return new UserCreateRecord(users, new HashSet<>(), permissionGroupList);
    }
}