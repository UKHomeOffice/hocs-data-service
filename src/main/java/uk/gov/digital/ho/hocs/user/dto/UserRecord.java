package uk.gov.digital.ho.hocs.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.user.model.User;

import java.io.Serializable;

@AllArgsConstructor
public class UserRecord implements Serializable {

    @Getter
    private String userName;

    @Getter
    private String firstName;

    @Getter
    private String lastName;

    @Getter
    private String email;

    public static UserRecord create(User user) {
        return new UserRecord(user.getUserName(), user.getFirstName(), user.getLastName(), user.getEmailAddress());
    }
}
