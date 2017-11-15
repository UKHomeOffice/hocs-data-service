package uk.gov.digital.ho.hocs;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.model.BusinessGroup;
import uk.gov.digital.ho.hocs.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest
@Profile("logtoconsole")
public class BusinessGroupRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessGroupRepository businessGroupRepository;

    @Before
    public void setup() {
        userRepository.deleteAll();
        businessGroupRepository.deleteAll();

        User userOne = new User("first1", "last", "user1", "email", "Dept2");
        userRepository.save(userOne);
        Set<User> users = new HashSet<>();
        users.add(userOne);

        BusinessGroup businessGroup = new BusinessGroup("Test");
        BusinessGroup subBusinessGroup = new BusinessGroup("SubTest");
        Set<BusinessGroup> subBusinessGroups = new HashSet<>();
        subBusinessGroups.add(subBusinessGroup);
        businessGroup.setSubGroups(subBusinessGroups);
        businessGroup.setUsers(users);

        businessGroupRepository.save(businessGroup);

        businessGroupRepository.save(new BusinessGroup("Test1"));
        businessGroupRepository.save(new BusinessGroup("Test2"));

    }

    @Test
    public void shouldRetrieveAllEntries() {
        final Iterable<BusinessGroup> all = businessGroupRepository.findAll();
        assertThat(all).size().isEqualTo(4);
    }

    @Test
    public void shouldRetrieveEntriesProperties() {
        final List<BusinessGroup> all = businessGroupRepository.findAll();
        assertThat(all).size().isEqualTo(4);
        BusinessGroup businessGroup = all.get(0);

        assertThat(businessGroup.getDisplayName()).isEqualTo("Test");
        assertThat(businessGroup.getReferenceName()).isEqualTo("GROUP_TEST");

        assertThat(businessGroup.getSubGroups()).hasSize(1);
        BusinessGroup subBusinessGroup = businessGroup.getSubGroups().toArray(new BusinessGroup[0])[0];
        assertThat(subBusinessGroup.getDisplayName()).isEqualTo("SubTest");
        assertThat(subBusinessGroup.getReferenceName()).isEqualTo("GROUP_SUBTEST");

        assertThat(businessGroup.getUsers()).hasSize(1);
        User firstUser = businessGroup.getUsers().toArray(new User[0])[0];
        assertThat(firstUser.getFirstName()).isEqualTo("first1");
        assertThat(firstUser.getLastName()).isEqualTo("last");
        assertThat(firstUser.getUserName()).isEqualTo("user1");
        assertThat(firstUser.getEmailAddress()).isEqualTo("email");
        assertThat(firstUser.getDepartment()).isEqualTo("Dept2");

    }

    @Test
    public void shouldRetrieveAllEntryFindByReference() {
        final BusinessGroup businessGroup = businessGroupRepository.findByReferenceName("GROUP_TEST");
        assertThat(businessGroup.getDisplayName()).isEqualTo("Test");
        assertThat(businessGroup.getReferenceName()).isEqualTo("GROUP_TEST");
    }

    @Test
    public void shouldRetrieveAllEntryFindByReferenceAddedAsSubGroup() {
        final BusinessGroup businessGroup = businessGroupRepository.findByReferenceName("GROUP_SUBTEST");
        assertThat(businessGroup.getDisplayName()).isEqualTo("SubTest");
        assertThat(businessGroup.getReferenceName()).isEqualTo("GROUP_SUBTEST");
    }

    @Test
    public void shouldRetrieveEntryFindByReferenceNoneFound() {
        final BusinessGroup businessGroup = businessGroupRepository.findByReferenceName("GROUP_NOT_FOUND");
        assertThat(businessGroup).isNull();
    }

}