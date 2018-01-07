package uk.gov.digital.ho.hocs.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.businessGroups.model.BusinessTeam;
import uk.gov.digital.ho.hocs.user.model.User;

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
        List<String> groups = user.getTeams().stream().map(BusinessTeam::getReferenceName).collect(Collectors.toList());
        return new PublishUserRecord(user.getFirstName(), user.getLastName(), user.getUserName(), user.getEmailAddress(), "Password1", groups);
    }
}
