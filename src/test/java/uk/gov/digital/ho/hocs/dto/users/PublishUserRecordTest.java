package uk.gov.digital.ho.hocs.dto.users;

import org.junit.Test;
import uk.gov.digital.ho.hocs.exception.GroupCreationException;
import uk.gov.digital.ho.hocs.model.BusinessGroup;
import uk.gov.digital.ho.hocs.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PublishUserRecordTest {
    @Test
    public void createUser() throws Exception, GroupCreationException {

        BusinessGroup group = new BusinessGroup("UnitDisp", "UnitRef");
        Set<BusinessGroup> groups = new HashSet<>();
        groups.add(group);

        User user = new User("first", "last", "user","email", "Dept");
        user.setGroups(groups);
        PublishUserRecord entityRecord = PublishUserRecord.create(user);

        assertThat(entityRecord.getFirstName()).isEqualTo("first");
        assertThat(entityRecord.getLastName()).isEqualTo("last");
        assertThat(entityRecord.getUserName()).isEqualTo("user");
        assertThat(entityRecord.getEmail()).isEqualTo("email");
        assertThat(entityRecord.getPassword()).isEqualTo("Password1");
        assertThat(entityRecord.getGroupNameArray()).hasSize(1);
    }

    @Test
    public void getStrings() throws Exception {
        List<String> groups = new ArrayList<>();
        groups.add("group");
        PublishUserRecord entityRecord = new PublishUserRecord("first", "last", "user", "email", "pass", groups);

        assertThat(entityRecord.getFirstName()).isEqualTo("first");
        assertThat(entityRecord.getLastName()).isEqualTo("last");
        assertThat(entityRecord.getUserName()).isEqualTo("user");
        assertThat(entityRecord.getEmail()).isEqualTo("email");
        assertThat(entityRecord.getPassword()).isEqualTo("pass");
        assertThat(entityRecord.getGroupNameArray()).hasSize(1);
    }

}