package uk.gov.digital.ho.hocs.user.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.businessGroups.model.BusinessTeam;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.user.model.User;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class PublishUserRecordTest {
    @Test
    public void createUser() throws EntityCreationException {

        BusinessTeam group = new BusinessTeam("UnitDisp", "UnitRef");
        Set<BusinessTeam> groups = new HashSet<>();
        groups.add(group);

        User user = new User("first", "last", "user","email", "Dept");
        user.setTeams(groups);
        PublishUserRecord entityRecord = PublishUserRecord.create(user);

        assertThat(entityRecord.getFirstName()).isEqualTo("first");
        assertThat(entityRecord.getLastName()).isEqualTo("last");
        assertThat(entityRecord.getUserName()).isEqualTo("user");
        assertThat(entityRecord.getEmail()).isEqualTo("email");
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