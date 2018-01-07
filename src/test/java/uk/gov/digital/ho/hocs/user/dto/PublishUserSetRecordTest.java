package uk.gov.digital.ho.hocs.user.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.user.model.User;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PublishUserSetRecordTest {

    @Test
    public void createWithEntities() throws Exception {
        Set<User> userList = new HashSet<>();
        userList.add(new User());
        PublishUserListRecord record = PublishUserListRecord.create(userList);
        assertThat(record.getUsers()).hasSize(1);
    }

    @Test
    public void createWithoutEntities() throws Exception {
        Set<User> userList = new HashSet<>();
        PublishUserListRecord record = PublishUserListRecord.create(userList);
        assertThat(record.getUsers()).hasSize(0);
    }
}