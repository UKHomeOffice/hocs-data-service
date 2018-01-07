package uk.gov.digital.ho.hocs.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.user.model.User;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public class UserSetRecord implements Serializable {

    @Getter
    private Set<UserRecord> users;

    public static UserSetRecord create(Set<User> list) {
        Set<UserRecord> users = list.stream().map(UserRecord::create).collect(Collectors.toSet());
        return new UserSetRecord(users);
    }
}