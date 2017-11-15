package uk.gov.digital.ho.hocs;

import org.assertj.core.api.Assertions;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;
import uk.gov.digital.ho.hocs.dto.legacy.users.UserCreateRecord;
import uk.gov.digital.ho.hocs.dto.legacy.users.UserRecord;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.legacy.users.CSVUserLine;
import uk.gov.digital.ho.hocs.model.BusinessGroup;
import uk.gov.digital.ho.hocs.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    private final static String GROUP_REF = "GROUP_REF";
    private final static String UNAVAILABLE_RESOURCE = "MISSING_REF";

    @Mock
    private UserRepository mockUserRepo;

    @Mock
    private BusinessGroupService mockBusinessGroupService;

    private UserService service;


    @Before
    public void setUp() {

        service = new UserService(mockUserRepo, mockBusinessGroupService);
    }

    @Test
    public void testCollaboratorsGettingGroupRefList() throws ListNotFoundException {
        when(mockUserRepo.findAllByBusinessGroupReference(GROUP_REF)).thenReturn(buildValidUserList());

        UserRecord userRecord = service.getUsersByGroupName(GROUP_REF);

        verify(mockUserRepo).findAllByBusinessGroupReference(GROUP_REF);

        assertThat(userRecord).isNotNull();
        assertThat(userRecord).isInstanceOf(UserRecord.class);
        Assertions.assertThat(userRecord.getUsers()).size().isEqualTo(1);
        assertThat(userRecord.getUsers().get(0).getUserName()).isEqualTo("User");
    }

    @Test(expected = ListNotFoundException.class)
    public void testGroupListNotFoundThrowsListNotFoundException() throws ListNotFoundException {
        UserRecord userRecord = service.getUsersByGroupName(UNAVAILABLE_RESOURCE);

        verify(mockUserRepo).findAllByBusinessGroupReference(UNAVAILABLE_RESOURCE);
        assertThat(userRecord).isNull();
    }

    @Test
    public void testCollaboratorsGettingDeptList() throws ListNotFoundException {
        when(mockUserRepo.findAllByDepartment(GROUP_REF)).thenReturn(buildValidUserList());

        UserCreateRecord userRecord = service.getUsersByDepartmentName(GROUP_REF);

        verify(mockUserRepo).findAllByDepartment(GROUP_REF);

        assertThat(userRecord).isNotNull();
        assertThat(userRecord).isInstanceOf(UserCreateRecord.class);
        Assertions.assertThat(userRecord.getUsers()).size().isEqualTo(1);
        assertThat(userRecord.getUsers().get(0).getUserName()).isEqualTo("User");
    }

    @Test(expected = ListNotFoundException.class)
    public void testBusinessListNotFoundThrowsListNotFoundException() throws ListNotFoundException {
        UserCreateRecord userRecord = service.getUsersByDepartmentName(UNAVAILABLE_RESOURCE);

        verify(mockUserRepo).findAllByDepartment(UNAVAILABLE_RESOURCE);
        assertThat(userRecord).isNull();
    }

    @Test
    public void testServiceCreatesUsersFromCSV() throws ListNotFoundException{
        when(mockBusinessGroupService.getGroupByReference("A_GROUP")).thenReturn(new BusinessGroup());

        List<String> groups = new ArrayList<>();
        groups.add("A_GROUP");
        CSVUserLine line = new CSVUserLine("First", "Last", "email", groups);
        Set<CSVUserLine> lines = new HashSet<>();
        lines.add(line);

        service.createUsersFromCSV(lines, "Dept");

        verify(mockBusinessGroupService, times(1)).getGroupByReference("A_GROUP");
        verify(mockUserRepo).save(anyList());
    }

    @Test
    public void testServiceCreatesUsersFromCSVNoUsers()throws ListNotFoundException {
        when(mockBusinessGroupService.getGroupByReference("Invalid_Group")).thenThrow(new ListNotFoundException());

        Set<CSVUserLine> lines = new HashSet<>();

        service.createUsersFromCSV(lines, "Dept");

        verify(mockBusinessGroupService, times(0)).getGroupByReference("Invalid_Group");
        verify(mockUserRepo, times(0)).save(anyList());
    }

    @Test(expected = ListNotFoundException.class)
    public void testServiceCreatesUsersFromCSVInvalidGroup()throws ListNotFoundException {
        when(mockBusinessGroupService.getGroupByReference("Invalid_Group")).thenThrow(new ListNotFoundException());

        List<String> groups = new ArrayList<>();
        groups.add("Invalid_Group");
        CSVUserLine line = new CSVUserLine("First", "Last", "email", groups);
        Set<CSVUserLine> lines = new HashSet<>();
        lines.add(line);

        service.createUsersFromCSV(lines, "Dept");

        verify(mockBusinessGroupService, times(1)).getGroupByReference("Invalid_Group");
        verify(mockUserRepo, times(0)).save(anyList());
    }

    @Test(expected = EntityCreationException.class)
    public void testRepoDataIntegrityExceptionThrowsEntityCreationException() throws ListNotFoundException {
        when(mockUserRepo.save(anyList())).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "user_name_idempotent")));

        List<String> groups = new ArrayList<>();
        groups.add("Invalid_Group");
        CSVUserLine line = new CSVUserLine("First", "Last", "email", groups);
        Set<CSVUserLine> lines = new HashSet<>();
        lines.add(line);

        service.createUsersFromCSV(lines, "Dept");

        verify(mockUserRepo).save(anyList());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testRepoUnhandledExceptionThrowsDataIntegrityException() throws ListNotFoundException {
        when(mockUserRepo.save(anyList())). thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "")));

        List<String> groups = new ArrayList<>();
        groups.add("Invalid_Group");
        CSVUserLine line = new CSVUserLine("First", "Last", "email", groups);
        Set<CSVUserLine> lines = new HashSet<>();
        lines.add(line);

        service.createUsersFromCSV(lines, "Dept");

        verify(mockUserRepo).save(anyList());
    }

    public List<User>buildValidUserList(){
        List<User> userList = new ArrayList<>();
        User user = new User("First", "Last", "User","email", "Dept");
        userList.add(user);
        return userList;
    }

}