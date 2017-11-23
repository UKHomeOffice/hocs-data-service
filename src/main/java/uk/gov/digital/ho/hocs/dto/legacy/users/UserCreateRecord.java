package uk.gov.digital.ho.hocs.dto.legacy.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.User;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class UserCreateRecord {
    private Set<UserCreateEntityRecord> users;

    public static UserCreateRecord create(Set<User> list) {
        Set<UserCreateEntityRecord> users = list.stream().map(UserCreateEntityRecord::create).collect(Collectors.toSet());
        return new UserCreateRecord(users);
    }
}