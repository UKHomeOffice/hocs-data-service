package uk.gov.digital.ho.hocs.dto.users;

import org.junit.Test;
import uk.gov.digital.ho.hocs.model.User;

import static org.assertj.core.api.Assertions.assertThat;

public class UserRecordTest {
    @Test
    public void createUser() throws Exception {
        User user = new User("First", "Last", "User","email", "Dept");
        UserRecord userEntity = UserRecord.create(user);

        assertThat(userEntity.getUserName()).isEqualTo("User");
        assertThat(userEntity.getFirstName()).isEqualTo("First");
        assertThat(userEntity.getLastName()).isEqualTo("Last");
        assertThat(userEntity.getEmail()).isEqualTo("email");
    }

    @Test
    public void getStrings() throws Exception {
        UserRecord userEntity = new UserRecord("User", "First", "Last", "email");

        assertThat(userEntity.getUserName()).isEqualTo("User");
        assertThat(userEntity.getFirstName()).isEqualTo("First");
        assertThat(userEntity.getLastName()).isEqualTo("Last");
        assertThat(userEntity.getEmail()).isEqualTo("email");
    }

}