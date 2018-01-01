package uk.gov.digital.ho.hocs.dto.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.User;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class PublishUserRecord implements Serializable {

    @Getter
    private String firstName;

    @Getter
    private String lastName;

    @Getter
    private String userName;

    @Getter
    private String email;

    // Passwords are expired when they are created
    @Getter
    private String password;

    @Getter
    private List<String> groupNameArray;

    public static PublishUserRecord create(User user) {
        List<String> groups = user.getGroups().stream().map(g -> g.getReferenceName()).collect(Collectors.toList());
        return new PublishUserRecord(user.getFirstName(), user.getLastName(), user.getUserName(), user.getEmailAddress(), "Password1", groups);
    }
}
