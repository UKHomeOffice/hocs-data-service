package uk.gov.digital.ho.hocs.user;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.businessGroups.BusinessTeamRepository;
import uk.gov.digital.ho.hocs.businessGroups.model.BusinessTeam;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.user.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest
@Profile("logtoconsole")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessTeamRepository businessTeamRepository;

    @Before
    public void setup() throws EntityCreationException {
        userRepository.deleteAll();
        businessTeamRepository.deleteAll();

        BusinessTeam businessTeam = new BusinessTeam("Test");
        businessTeamRepository.save(businessTeam);

        User userOne = new User("first1", "last", "user1","email", "Dept2");
        Set<BusinessTeam> businessTeams = new HashSet<>();
        businessTeams.add(businessTeam);
        userOne.setTeams(businessTeams);

        userRepository.save(userOne);
        userRepository.save(new User("first2", "last", "user2","email", "Dept"));
        userRepository.save(new User("first3", "last", "user3","email", "Dept"));
    }

    @Test
    public void shouldRetrieveAllEntries() {
        final Iterable<User> all = userRepository.findAllByDepartment("Dept");
        assertThat(all).size().isEqualTo(2);
    }

    @Test
    public void shouldRetrieveEntriesProperties() {
        final Set<User> all = userRepository.findAllByDepartment("Dept2");
        assertThat(all).size().isEqualTo(1);
        User firstUser = new ArrayList<>(all).get(0);

        assertThat(firstUser.getFirstName()).isEqualTo("first1");
        assertThat(firstUser.getLastName()).isEqualTo("last");
        assertThat(firstUser.getUserName()).isEqualTo("user1");
        assertThat(firstUser.getEmailAddress()).isEqualTo("email");
        assertThat(firstUser.getDepartment()).isEqualTo("Dept2");
        assertThat(firstUser.getTeams()).hasSize(1);
        assertThat(firstUser.getTeams().toArray(new BusinessTeam[0])[0].getReferenceName()).isEqualTo("GROUP_TEST");
    }

    @Test
    public void shouldRetrieveAllEntriesNoneFound() {
        final Iterable<User> all = userRepository.findAllByDepartment("NoEntries");
        assertThat(all).size().isEqualTo(0);
    }

    @Test
    public void shouldRetrieveByBusinessGroupRef() {
        final Iterable<User> all = userRepository.findAllByBusinessGroupReference("GROUP_TEST");
        assertThat(all).size().isEqualTo(1);
    }

    @Test
    public void shouldRetrieveByBusinessGroupRefNone() {
        final Iterable<User> all = userRepository.findAllByBusinessGroupReference("GROUP_TEST_NO_ENTRIES");
        assertThat(all).isEmpty();
    }

    @Test
    public void shouldRetrieveEntriesByBusinessGroupProperties() {
        final Set<User> all = userRepository.findAllByBusinessGroupReference("GROUP_TEST");
        assertThat(all).size().isEqualTo(1);
        User firstUser = new ArrayList<>(all).get(0);

        assertThat(firstUser.getFirstName()).isEqualTo("first1");
        assertThat(firstUser.getLastName()).isEqualTo("last");
        assertThat(firstUser.getUserName()).isEqualTo("user1");
        assertThat(firstUser.getEmailAddress()).isEqualTo("email");
        assertThat(firstUser.getDepartment()).isEqualTo("Dept2");
        assertThat(firstUser.getTeams()).hasSize(1);
        assertThat(firstUser.getTeams().toArray(new BusinessTeam[0])[0].getReferenceName()).isEqualTo("GROUP_TEST");
    }

}