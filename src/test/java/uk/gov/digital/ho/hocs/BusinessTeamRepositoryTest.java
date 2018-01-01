package uk.gov.digital.ho.hocs;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.exception.GroupCreationException;
import uk.gov.digital.ho.hocs.model.BusinessTeam;
import uk.gov.digital.ho.hocs.model.BusinessUnit;
import uk.gov.digital.ho.hocs.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest
@Profile("logtoconsole")
public class BusinessTeamRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessUnitRepository businessUnitRepository;

    @Autowired
    private BusinessTeamRepository businessTeamRepository;

    @Before
    public void setup() throws GroupCreationException {
        userRepository.deleteAll();
        businessUnitRepository.deleteAll();

        User userOne = new User("first1", "last", "user1", "email", "Dept2");
        userRepository.save(userOne);
        Set<User> users = new HashSet<>();
        users.add(userOne);

        BusinessUnit businessUnit = new BusinessUnit("Test");
        BusinessTeam subBusinessTeam = new BusinessTeam("SubTest");
        subBusinessTeam.setUsers(users);
        Set<BusinessTeam> subBusinessTeams = new HashSet<>();
        subBusinessTeams.add(subBusinessTeam);
        businessUnit.setTeams(subBusinessTeams);

        businessUnitRepository.save(businessUnit);

        businessUnitRepository.save(new BusinessUnit("Test1"));
        businessUnitRepository.save(new BusinessUnit("Test2"));

    }

    @Test
    public void shouldRetrieveAllEntries() {
        final Iterable<BusinessUnit> all = businessUnitRepository.findAll();
        assertThat(all).size().isEqualTo(3);
    }

    @Test
    public void shouldRetrieveAllEntriesNotDeleted() {
        final Iterable<BusinessUnit> all = businessUnitRepository.findAllByDeletedIsFalse();
        assertThat(all).size().isEqualTo(3);
    }

    @Test
    public void shouldRetrieveEntriesProperties() {
        final Set<BusinessUnit> all = businessUnitRepository.findAll();
        assertThat(all).size().isEqualTo(3);
        BusinessUnit businessUnit = new ArrayList<>(all).get(0);

        assertThat(businessUnit.getDisplayName()).isEqualTo("Test");
        assertThat(businessUnit.getReferenceName()).isEqualTo("GROUP_TEST");

        assertThat(businessUnit.getTeams()).hasSize(1);
        BusinessTeam subBusinessTeam = businessUnit.getTeams().toArray(new BusinessTeam[0])[0];
        assertThat(subBusinessTeam.getDisplayName()).isEqualTo("SubTest");
        assertThat(subBusinessTeam.getReferenceName()).isEqualTo("GROUP_SUBTEST");
        assertThat(subBusinessTeam.getUsers()).hasSize(1);
        User firstUser = subBusinessTeam.getUsers().toArray(new User[0])[0];
        assertThat(firstUser.getFirstName()).isEqualTo("first1");
        assertThat(firstUser.getLastName()).isEqualTo("last");
        assertThat(firstUser.getUserName()).isEqualTo("user1");
        assertThat(firstUser.getEmailAddress()).isEqualTo("email");
        assertThat(firstUser.getDepartment()).isEqualTo("Dept2");

    }

    // This functionality isn't required yet.
    //@Test
    //public void shouldRetrieveAllEntryFindByReference() {
    //    final BusinessTeam businessTeam = businessTeamRepository.findOneByReferenceNameAndDeletedIsFalse("GROUP_TEST");
    //    assertThat(businessTeam.getDisplayName()).isEqualTo("Test");
    //    assertThat(businessTeam.getReferenceName()).isEqualTo("GROUP_TEST");
    //}

    @Test
    public void shouldRetrieveAllEntryFindByReferenceAddedAsSubGroup() {
        final BusinessTeam businessTeam = businessTeamRepository.findOneByReferenceNameAndDeletedIsFalse("GROUP_SUBTEST");
        assertThat(businessTeam.getDisplayName()).isEqualTo("SubTest");
        assertThat(businessTeam.getReferenceName()).isEqualTo("GROUP_SUBTEST");
    }

    @Test
    public void shouldRetrieveEntryFindByReferenceNoneFound() {
        final BusinessTeam businessTeam = businessTeamRepository.findOneByReferenceNameAndDeletedIsFalse("GROUP_NOT_FOUND");
        assertThat(businessTeam).isNull();
    }

}