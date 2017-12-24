package uk.gov.digital.ho.hocs.dto.legacy.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.User;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class UserCreateEntityRecord implements Serializable {

    private String firstName;

    private String lastName;

    private String userName;

    private String email;

    // Passwords are expired when they are created
    private String password;

    private List<String> groupNameArray;

    public static UserCreateEntityRecord create(User user) {
        List<String> groups = user.getGroups().stream().map(g -> g.getReferenceName()).collect(Collectors.toList());
        return new UserCreateEntityRecord(user.getFirstName(), user.getLastName(), user.getUserName(), user.getEmailAddress(), "Password1", groups);
    }
}
