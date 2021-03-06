package uk.gov.digital.ho.hocs.user;

import org.assertj.core.api.Assertions;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;
import uk.gov.digital.ho.hocs.AlfrescoClient;
import uk.gov.digital.ho.hocs.businessGroups.BusinessUnitService;
import uk.gov.digital.ho.hocs.businessGroups.model.BusinessTeam;
import uk.gov.digital.ho.hocs.exception.AlfrescoPostException;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.user.dto.UserSetRecord;
import uk.gov.digital.ho.hocs.user.ingest.CSVUserLine;
import uk.gov.digital.ho.hocs.user.model.User;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

@RunWith(org.mockito.junit.MockitoJUnitRunner.Silent.class)
public class UserServiceTest {

    private final static String GROUP_REF = "GROUP_REF";
    private final static String UNAVAILABLE_RESOURCE = "MISSING_REF";

    @Mock
    private UserRepository mockUserRepo;

    @Mock
    private BusinessUnitService mockBusinessUnitService;

    @Mock
    private AlfrescoClient mockAlfrescoClient;

    private UserService service;


    @Before
    public void setUp() {
        service = new UserService(mockUserRepo, mockBusinessUnitService, mockAlfrescoClient);
    }

    @Test
    public void testCollaboratorsGettingGroupRefList() throws EntityNotFoundException {
        when(mockUserRepo.findAllByBusinessGroupReference(GROUP_REF)).thenReturn(buildValidUserList());

        UserSetRecord userSetRecord = service.getUsersByGroupName(GROUP_REF);

        verify(mockUserRepo).findAllByBusinessGroupReference(GROUP_REF);

        assertThat(userSetRecord).isNotNull();
        assertThat(userSetRecord).isInstanceOf(UserSetRecord.class);
        Assertions.assertThat(userSetRecord.getUsers()).size().isEqualTo(1);
        assertThat(new ArrayList<>(userSetRecord.getUsers()).get(0).getUserName()).isEqualTo("User");
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGroupListNotFoundThrowsListNotFoundException() throws EntityNotFoundException {
        UserSetRecord userSetRecord = service.getUsersByGroupName(UNAVAILABLE_RESOURCE);

        verify(mockUserRepo).findAllByBusinessGroupReference(UNAVAILABLE_RESOURCE);
        assertThat(userSetRecord).isNull();
    }

    @Test
    public void testCollaboratorsGettingDeptList() throws EntityNotFoundException {
        when(mockUserRepo.findAllByDepartment(GROUP_REF)).thenReturn(buildValidUserList());

        UserSetRecord userRecord = service.getUsersByDepartmentName(GROUP_REF);

        verify(mockUserRepo).findAllByDepartment(GROUP_REF);

        assertThat(userRecord).isNotNull();
        assertThat(userRecord).isInstanceOf(UserSetRecord.class);
        Assertions.assertThat(userRecord.getUsers()).size().isEqualTo(1);
        assertThat(new ArrayList<>(userRecord.getUsers()).get(0).getUserName()).isEqualTo("User");
    }

    @Test(expected = EntityNotFoundException.class)
    public void testBusinessListNotFoundThrowsListNotFoundException() throws EntityNotFoundException {
        UserSetRecord userRecord = service.getUsersByDepartmentName(UNAVAILABLE_RESOURCE);

        verify(mockUserRepo).findAllByDepartment(UNAVAILABLE_RESOURCE);
        assertThat(userRecord).isNull();
    }

    @Test
    public void testServiceCreatesUsersFromCSV() throws EntityNotFoundException, EntityCreationException {
        when(mockBusinessUnitService.getTeamByReference("A_GROUP")).thenReturn(Stream.of(new BusinessTeam("disp")).collect(Collectors.toSet()));

        List<String> groups = new ArrayList<>();
        groups.add("A_GROUP");
        CSVUserLine line = new CSVUserLine("First", "Last", "email", groups);
        Set<CSVUserLine> lines = new HashSet<>();
        lines.add(line);

        service.updateUsersByDepartment(lines, "Dept");

        verify(mockBusinessUnitService, times(1)).getTeamByReference("A_GROUP");
        verify(mockUserRepo).saveAll(any());
    }

    @Test
    public void testServiceCreatesUsersFromCSVNoUsers() throws EntityNotFoundException {
        when(mockBusinessUnitService.getTeamByReference("Invalid_Group")).thenThrow(new EntityNotFoundException());

        Set<CSVUserLine> lines = new HashSet<>();

        service.updateUsersByDepartment(lines, "Dept");

        verify(mockBusinessUnitService, times(0)).getTeamByReference("Invalid_Group");
        verify(mockUserRepo, times(0)).saveAll(anyList());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testServiceCreatesUsersFromCSVInvalidGroup() throws EntityNotFoundException {
        when(mockBusinessUnitService.getTeamByReference("Invalid_Group")).thenThrow(new EntityNotFoundException());

        List<String> groups = new ArrayList<>();
        groups.add("Invalid_Group");
        CSVUserLine line = new CSVUserLine("First", "Last", "email", groups);
        Set<CSVUserLine> lines = new HashSet<>();
        lines.add(line);

        service.updateUsersByDepartment(lines, "Dept");

        verify(mockBusinessUnitService, times(1)).getTeamByReference("Invalid_Group");
        verify(mockUserRepo, times(0)).saveAll(anyList());
    }

    @Test(expected = EntityCreationException.class)
    public void testRepoDataIntegrityExceptionThrowsEntityCreationException() throws EntityNotFoundException {
        when(mockUserRepo.saveAll(any())).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "user_name_idempotent")));

        List<String> groups = new ArrayList<>();
        groups.add("Invalid_Group");
        CSVUserLine line = new CSVUserLine("First", "Last", "email", groups);
        Set<CSVUserLine> lines = new HashSet<>();
        lines.add(line);

        service.updateUsersByDepartment(lines, "Dept");

        verify(mockUserRepo).saveAll(any());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testRepoUnhandledExceptionThrowsDataIntegrityException() throws EntityNotFoundException {
        when(mockUserRepo.saveAll(any())). thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "")));

        List<String> groups = new ArrayList<>();
        groups.add("Invalid_Group");
        CSVUserLine line = new CSVUserLine("First", "Last", "email", groups);
        Set<CSVUserLine> lines = new HashSet<>();
        lines.add(line);

        service.updateUsersByDepartment(lines, "Dept");

        verify(mockUserRepo).saveAll(any());
    }

    @Test
    public void testServiceUpdateUsersFromCSVAdd() throws EntityNotFoundException, EntityCreationException {
        when(mockBusinessUnitService.getTeamByReference("A_GROUP")).thenReturn(new HashSet<>(Collections.singletonList(new BusinessTeam("disp"))));
        Set<User> users = new HashSet<>();
        User userOne = new User("First1", "Last1", "Email1", "Email1", "Dept");
        User userTwo = new User("First2", "Last2", "Email2", "Email2", "Dept");
        users.add(userOne);
        users.add(userTwo);
        when(mockUserRepo.findAllByDepartment("Dept")).thenReturn(users);

        List<String> groups = new ArrayList<>();
        groups.add("A_GROUP");
        CSVUserLine lineOne =   new CSVUserLine("First1", "Last1", "Email1", groups);
        CSVUserLine lineTwo =   new CSVUserLine("First2", "Last2", "Email2", groups);
        CSVUserLine lineThree = new CSVUserLine("First3", "Last3", "Email3", groups);
        Set<CSVUserLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineTwo);
        lines.add(lineThree);
        service.updateUsersByDepartment(lines, "Dept");

        verify(mockUserRepo, times(1)).saveAll(any());
        verify(mockUserRepo, times(0)).deleteAll(anyList());
    }

    @Test
    public void testServiceUpdateUsersFromCSVRemove() throws EntityNotFoundException, EntityCreationException {
        when(mockBusinessUnitService.getTeamByReference("A_GROUP")).thenReturn(new HashSet<>(Collections.singletonList(new BusinessTeam("disp"))));
        Set<User> users = new HashSet<>();
        User userOne = new User("First1", "Last1", "Email1", "Email1", "Dept");
        User userTwo = new User("First2", "Last2", "Email2", "Email2", "Dept");
        users.add(userOne);
        users.add(userTwo);
        when(mockUserRepo.findAllByDepartment("Dept")).thenReturn(users);

        List<String> groups = new ArrayList<>();
        groups.add("A_GROUP");
        CSVUserLine lineOne =   new CSVUserLine("First1", "Last1", "Email1", groups);
        Set<CSVUserLine> lines = new HashSet<>();
        lines.add(lineOne);
        service.updateUsersByDepartment(lines, "Dept");

        verify(mockUserRepo, times(0)).saveAll(anyList());
        verify(mockUserRepo, times(1)).deleteAll(any());
    }

    @Test
    public void testServiceUpdateUsersFromCSVBoth() throws EntityNotFoundException, EntityCreationException {
        when(mockBusinessUnitService.getTeamByReference("A_GROUP")).thenReturn(new HashSet<>(Collections.singletonList(new BusinessTeam("disp"))));
        Set<User> users = new HashSet<>();
        User userOne = new User("First1", "Last1", "Email1", "Email1", "Dept");
        User userTwo = new User("First2", "Last2", "Email2", "Email2", "Dept");
        users.add(userOne);
        users.add(userTwo);
        when(mockUserRepo.findAllByDepartment("Dept")).thenReturn(users);

        List<String> groups = new ArrayList<>();
        groups.add("A_GROUP");
        CSVUserLine lineOne =   new CSVUserLine("First1", "Last1", "Email1", groups);
        CSVUserLine lineThree = new CSVUserLine("First3", "Last3", "Email3", groups);
        Set<CSVUserLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineThree);
        service.updateUsersByDepartment(lines, "Dept");

        verify(mockUserRepo, times(1)).saveAll(any());
        verify(mockUserRepo, times(1)).deleteAll(any());
    }

    @Test
    public void testServiceUpdateUsersFromCSVNothingSame() throws EntityNotFoundException, EntityCreationException {
        when(mockBusinessUnitService.getTeamByReference("A_GROUP")).thenReturn(new HashSet<>(Collections.singletonList(new BusinessTeam("disp"))));
        Set<User> users = new HashSet<>();
        User userOne = new User("First1", "Last1", "Email1", "Email1", "Dept");
        User userTwo = new User("First2", "Last2", "Email2", "Email2", "Dept");
        users.add(userOne);
        users.add(userTwo);
        when(mockUserRepo.findAllByDepartment("Dept")).thenReturn(users);

        List<String> groups = new ArrayList<>();
        groups.add("A_GROUP");
        CSVUserLine lineOne =   new CSVUserLine("First1", "Last1", "Email1", groups);
        CSVUserLine lineTwo =   new CSVUserLine("First2", "Last2", "Email2", groups);
        Set<CSVUserLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineTwo);
        service.updateUsersByDepartment(lines, "Dept");

        verify(mockUserRepo, times(0)).saveAll(anyList());
        verify(mockUserRepo, times(0)).deleteAll(anyList());
    }

    @Test
    public void testServiceUpdateUsersFromCSVNothingNone() throws EntityNotFoundException, EntityCreationException {
        when(mockBusinessUnitService.getTeamByReference("A_GROUP")).thenReturn(new HashSet<>(Collections.singletonList(new BusinessTeam("disp"))));
        Set<User> users = new HashSet<>();
        User userOne = new User("First1", "Last1", "Email1", "Email1", "Dept");
        User userTwo = new User("First2", "Last2", "Email2", "Email2", "Dept");
        users.add(userOne);
        users.add(userTwo);
        when(mockUserRepo.findAllByDepartment("Dept")).thenReturn(users);

        List<String> groups = new ArrayList<>();
        groups.add("A_GROUP");
        Set<CSVUserLine> lines = new HashSet<>();
        service.updateUsersByDepartment(lines, "Dept");

        verify(mockUserRepo, times(0)).saveAll(anyList());
        verify(mockUserRepo, times(1)).deleteAll(any());
    }

    public Set<User>buildValidUserList(){
        Set<User> users = new HashSet<>();
        User user = new User("First", "Last", "User","email", "Dept");
        users.add(user);
        return users;
    }

    @Test
    public void testPublishUsersByDepartmentName() throws AlfrescoPostException, EntityNotFoundException {
        final Set<User> testUsers = generateTestUsers(154);
        when(mockUserRepo.findAllByDepartment("test_users")).thenReturn(testUsers);

        service.publishUsersByDepartmentName("test_users");

        verify(mockAlfrescoClient, times(1)).postUsers(anyList());

    }

    @Test(expected = EntityNotFoundException.class)
    public void testPublishUsersByDepartmentNameNoUsers() throws AlfrescoPostException, EntityNotFoundException {
        when(mockUserRepo.findAllByDepartment("test_users")).thenReturn(new HashSet<>());

        service.publishUsersByDepartmentName("test_users");

        verify(mockAlfrescoClient, times(0)).postUsers(anyList());

    }

    private Set<User> generateTestUsers(int quantity) {
        Set<User> users = new HashSet<>();
        for (int i = 0; i < quantity; i++) {
            users.add(new User("Test", String.format("User %s", i), String.format("TestUser%s", i), "Test.User@test.com", "test_users"));
        }
        return users;
    }

}