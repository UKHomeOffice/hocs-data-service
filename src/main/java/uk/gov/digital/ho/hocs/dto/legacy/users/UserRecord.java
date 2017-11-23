package uk.gov.digital.ho.hocs.dto.legacy.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.User;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class UserRecord {
    private Set<UserEntityRecord> users;

    public static UserRecord create(Set<User> list) {
        Set<UserEntityRecord> users = list.stream().map(UserEntityRecord::create).collect(Collectors.toSet());
        return new UserRecord(users);
    }
}