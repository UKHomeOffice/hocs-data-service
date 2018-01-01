package uk.gov.digital.ho.hocs.dto.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import uk.gov.digital.ho.hocs.dto.units.PublishUnitEntityPermissionListRecord;
import uk.gov.digital.ho.hocs.dto.units.PublishUnitEntityPermissionRecord;
import uk.gov.digital.ho.hocs.dto.units.PublishUnitEntityRecord;
import uk.gov.digital.ho.hocs.model.User;

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

        return new PublishUserListRecord(users, new HashSet<>(), permissionGroupList);
    }
}
