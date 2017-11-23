package uk.gov.digital.ho.hocs.dto.legacy.users;

import org.junit.Test;
import uk.gov.digital.ho.hocs.model.User;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class UserRecordTest {

    @Test
    public void createWithEntities() throws Exception {
        Set<User> userList = new HashSet<>();
        userList.add(new User());
        UserRecord record = UserRecord.create(userList);
        assertThat(record.getUsers()).hasSize(1);
    }

    @Test
    public void createWithoutEntities() throws Exception {
        Set<User> userList = new HashSet<>();
        UserRecord record = UserRecord.create(userList);
        assertThat(record.getUsers()).hasSize(0);
    }

}