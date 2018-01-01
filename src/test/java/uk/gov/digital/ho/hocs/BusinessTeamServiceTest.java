package uk.gov.digital.ho.hocs;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.GroupCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.ingest.units.CSVBusinessGroupLine;
import uk.gov.digital.ho.hocs.model.BusinessTeam;
import uk.gov.digital.ho.hocs.model.BusinessUnit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BusinessTeamServiceTest {

    private final static String CASETYPE = "Test";
    private final static String UNAVAILABLE_RESOURCE = "Unavailable Resource";

    @Mock
    private BusinessUnitRepository mockUnitRepo;

    @Mock
    private BusinessTeamRepository mockTeamRepo;

    @Captor
    private ArgumentCaptor<HashSet<BusinessUnit>> captor;

    private BusinessGroupService BusinessGroupService;


    @Before
    public void setUp() {
        BusinessGroupService = new BusinessGroupService(mockUnitRepo, mockTeamRepo);
    }

    @Test
    public void testCollaboratorsGettingBusinessGroup() throws ListNotFoundException, GroupCreationException {
        BusinessTeam businessTeamOne = new BusinessTeam("businessGroupName");
        when(mockTeamRepo.findOneByReferenceNameAndDeletedIsFalse(CASETYPE)).thenReturn(businessTeamOne);

        BusinessTeam record = BusinessGroupService.getTeamByReference(CASETYPE);

        verify(mockTeamRepo).findOneByReferenceNameAndDeletedIsFalse(CASETYPE);

        assertThat(record).isNotNull();
        assertThat(record.getDisplayName()).isEqualTo("businessGroupName");
    }

    @Test(expected = ListNotFoundException.class)
    public void testLegacyListNotFoundThrowsListNotFoundException() throws ListNotFoundException {

        BusinessGroupService.getTeamByReference(UNAVAILABLE_RESOURCE);
        verify(mockUnitRepo).findAllByDeletedIsFalse();
    }

    @Test
    public void testCollaboratorsGettingAllBusinessGroup() throws ListNotFoundException, GroupCreationException {
        when(mockUnitRepo.findAllByDeletedIsFalse()).thenReturn(buildbusinessGroupList());

        List<BusinessUnit> records = BusinessGroupService.getAllBusinessUnits().stream().collect(Collectors.toList());

        verify(mockUnitRepo).findAllByDeletedIsFalse();

        assertThat(records).isNotNull();
        assertThat(records).hasOnlyElementsOfType(BusinessUnit.class);
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getDisplayName()).isEqualTo("businessGroupName");
    }

    @Test(expected = ListNotFoundException.class)
    public void testAllListNotFoundThrowsListNotFoundException() throws ListNotFoundException {

        List<BusinessUnit> records = BusinessGroupService.getAllBusinessUnits().stream().collect(Collectors.toList());
        verify(mockUnitRepo).findAllByDeletedIsFalse();
        assertThat(records).isEmpty();
    }

    @Test
    public void testCreateList() throws GroupCreationException {
        BusinessGroupService.updateBusinessUnits(buildValidCSVBusinessGroupLines());
        verify(mockUnitRepo).save(anyList());
    }

    @Test(expected = GroupCreationException.class)
    public void testCreateLisTooLong() throws GroupCreationException {
        Set<CSVBusinessGroupLine> lines = new HashSet<>();

        CSVBusinessGroupLine line = new CSVBusinessGroupLine("ParentbusinessGroupNametoolong", "ParentbusinessGroupNametooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooolong", "BusinessGroupUnit", "BusinessGroupTeam");
        lines.add(line);

        BusinessGroupService.updateBusinessUnits(lines);
        verify(mockUnitRepo, times(0)).save(anyList());
    }

    @Test(expected = EntityCreationException.class)
    public void testCreateListNull() throws GroupCreationException {
        BusinessGroupService.updateBusinessUnits(null);
        verify(mockUnitRepo, times(0)).save(anyList());
    }

    @Test
    public void testCreateListNoEntities() throws GroupCreationException {
        BusinessGroupService.updateBusinessUnits(new HashSet<>());
        verify(mockUnitRepo, times(0)).save(anyList());
    }

    @Test(expected = EntityCreationException.class)
    public void testRepoDataIntegrityExceptionThrowsEntityCreationException() throws GroupCreationException {

        Set<CSVBusinessGroupLine> BusinessGroup = buildValidCSVBusinessGroupLines();

        when(mockUnitRepo.save(anyList())).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "group_name_ref_idempotent")));
        BusinessGroupService.updateBusinessUnits(BusinessGroup);

        verify(mockUnitRepo).save(anyList());
    }

    @Test(expected = EntityCreationException.class)
    public void testRepoDataIntegrityExceptionThrowsEntityCreationExceptionTwo() throws GroupCreationException {

        Set<CSVBusinessGroupLine> BusinessGroup = buildValidCSVBusinessGroupLines();

        when(mockUnitRepo.save(anyList())).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "group_name_ref_idempotent")));
        BusinessGroupService.updateBusinessUnits(BusinessGroup);

        verify(mockUnitRepo).save(anyList());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testRepoDataIntegrityExceptionThrowsDataIntegrityViolationException() throws GroupCreationException {

        Set<CSVBusinessGroupLine> BusinessGroup = buildValidCSVBusinessGroupLines();

        when(mockUnitRepo.save(anyList())).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "")));
        BusinessGroupService.updateBusinessUnits(BusinessGroup);

        verify(mockUnitRepo).save(anyList());
    }

    @Test
    public void testServiceUpdateBusinessGroupFromCSVAdd() throws ListNotFoundException, GroupCreationException {
        Set<BusinessUnit> businessUnits = getBusinessGroups();
        when(mockUnitRepo.findAll()).thenReturn(businessUnits);

        CSVBusinessGroupLine lineOne = new CSVBusinessGroupLine("businessGroupName1", "businessGroupName1", "subBusinessGroupName1", "subBusinessGroupName1");
        CSVBusinessGroupLine lineTwo = new CSVBusinessGroupLine("businessGroupName2", "businessGroupName2", "subBusinessGroupName2", "subBusinessGroupName2");
        CSVBusinessGroupLine lineThree = new CSVBusinessGroupLine("businessGroupName3", "businessGroupName3", "subBusinessGroupName3", "subBusinessGroupName3");
        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineTwo);
        lines.add(lineThree);
        BusinessGroupService.updateBusinessUnits(lines);

        verify(mockUnitRepo, times(1)).save(anyList());
    }

    @Test
    public void testServiceUpdateBusinessGroupFromCsvAddWhenAlreadyDeleted() throws GroupCreationException {
        Set<BusinessUnit> businessUnits = getBusinessGroupsChildDeleted(true, true);
        when(mockUnitRepo.findAll()).thenReturn(businessUnits);

        CSVBusinessGroupLine lineOne = new CSVBusinessGroupLine("businessGroupName1", "businessGroupName1", "subBusinessGroupName1", "subBusinessGroupName1");
        CSVBusinessGroupLine lineTwo = new CSVBusinessGroupLine("businessGroupName2", "businessGroupName2", "subBusinessGroupName2", "subBusinessGroupName2");
        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineTwo);
        BusinessGroupService.updateBusinessUnits(lines);

        verify(mockUnitRepo).save(captor.capture());
        final Set<BusinessUnit> businessUnitList = captor.getValue();

        verify(mockUnitRepo, times(1)).save(anyList());
        assertThat(businessUnitList).isNotNull();
        assertThat(businessUnitList).hasSize(2);

        BusinessUnit businessUnit1 = getBusinessGroupByName(businessUnitList,"businessGroupName1");
        assertThat(businessUnit1).isNotNull();
        assertThat(businessUnit1.getDeleted()).isFalse();

        BusinessTeam subBusinessTeam1 = getBusinessGroupItemByName(businessUnit1.getTeams(), "subBusinessGroupName1");
        assertThat(subBusinessTeam1).isNotNull();
        assertThat(subBusinessTeam1.getDeleted()).isFalse();

        BusinessUnit businessUnit2 = getBusinessGroupByName(businessUnitList,"businessGroupName2");
        assertThat(businessUnit2).isNotNull();
        assertThat(businessUnit2.getDeleted()).isFalse();

        BusinessTeam subBusinessTeam2 = getBusinessGroupItemByName(businessUnit2.getTeams(), "subBusinessGroupName2");
        assertThat(subBusinessTeam2).isNotNull();
        assertThat(subBusinessTeam2.getDeleted()).isFalse();
    }

    @Test
    public void testServiceUpdateBusinessGroupFromCsvEmptyGroupIsDeleted() throws GroupCreationException {
        Set<BusinessUnit> businessUnits = getBusinessGroupsChildDeleted(true, true);
        when(mockUnitRepo.findAll()).thenReturn(businessUnits);

        CSVBusinessGroupLine lineOne = new CSVBusinessGroupLine("businessGroupName1", "businessGroupName1", "subBusinessGroupName4", "subBusinessGroupName4");
        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        lines.add(lineOne);
        BusinessGroupService.updateBusinessUnits(lines);

        verify(mockUnitRepo).save(captor.capture());
        final Set<BusinessUnit> businessUnitList = captor.getValue();

        verify(mockUnitRepo, times(1)).save(anyList());
        assertThat(businessUnitList).isNotNull();
        assertThat(businessUnitList).hasSize(1);

        BusinessUnit businessUnit1 = getBusinessGroupByName(businessUnitList,"businessGroupName1");
        assertThat(businessUnit1).isNotNull();
        assertThat(businessUnit1.getDeleted()).isFalse();

        BusinessTeam subBusinessTeam1 = getBusinessGroupItemByName(businessUnit1.getTeams(), "subBusinessGroupName1");
        assertThat(subBusinessTeam1).isNotNull();
        assertThat(subBusinessTeam1.getDeleted()).isTrue();

        BusinessTeam businessTeam2 = getBusinessGroupItemByName(businessUnit1.getTeams(), "subBusinessGroupName4");
        assertThat(businessTeam2).isNotNull();
        assertThat(businessTeam2.getDeleted()).isFalse();
    }

    @Test
    public void testServiceUpdateBusinessGroupFromCSVRemove() throws ListNotFoundException, GroupCreationException {
        Set<BusinessUnit> businessUnits = getBusinessGroups();
        when(mockUnitRepo.findAll()).thenReturn(businessUnits);

        CSVBusinessGroupLine lineOne = new CSVBusinessGroupLine("businessGroupName1", "businessGroupName1", "subBusinessGroupName1", "subBusinessGroupName1");
        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        lines.add(lineOne);
        BusinessGroupService.updateBusinessUnits(lines);

        verify(mockUnitRepo).save(captor.capture());
        final Set<BusinessUnit> businessUnitList = captor.getValue();

        verify(mockUnitRepo, times(1)).save(anyList());
        assertThat(businessUnitList).isNotNull();
        assertThat(businessUnitList).hasSize(2);

        BusinessUnit businessUnit1 = getBusinessGroupByName(businessUnitList,"businessGroupName1");
        assertThat(businessUnit1).isNotNull();
        assertThat(businessUnit1.getDeleted()).isFalse();

        BusinessTeam subBusinessTeam1 = getBusinessGroupItemByName(businessUnit1.getTeams(), "subBusinessGroupName1");
        assertThat(subBusinessTeam1).isNotNull();
        assertThat(subBusinessTeam1.getDeleted()).isFalse();

        BusinessUnit businessUnit2 = getBusinessGroupByName(businessUnitList,"businessGroupName2");
        assertThat(businessUnit2).isNotNull();
        assertThat(businessUnit2.getDeleted()).isTrue();

        BusinessTeam subBusinessTeam2 = getBusinessGroupItemByName(businessUnit2.getTeams(), "subBusinessGroupName2");
        assertThat(subBusinessTeam2).isNotNull();
        assertThat(subBusinessTeam2.getDeleted()).isTrue();

    }

    @Test
    public void testServiceUpdateBusinessGroupFromCSVRemoveChildBusinessGroup() throws ListNotFoundException, GroupCreationException {
        Set<BusinessUnit> businessUnits = getBusinessGroups();
        when(mockUnitRepo.findAll()).thenReturn(businessUnits);

        CSVBusinessGroupLine lineOne = new CSVBusinessGroupLine("businessGroupName1", "businessGroupName1", "subBusinessGroupName1", "subBusinessGroupName1");
        CSVBusinessGroupLine lineTwo = new CSVBusinessGroupLine("businessGroupName2", "businessGroupName2", "subBusinessGroupName2", "subBusinessGroupName2");
        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineTwo);
        BusinessGroupService.updateBusinessUnits(lines);

        verify(mockUnitRepo).save(captor.capture());
        final Set<BusinessUnit> businessUnitList = captor.getValue();

        verify(mockUnitRepo, times(1)).save(anyList());
        assertThat(businessUnitList).isNotNull();
        assertThat(businessUnitList).hasSize(2);

        BusinessUnit businessUnit1 = getBusinessGroupByName(businessUnitList,"businessGroupName1");
        assertThat(businessUnit1).isNotNull();
        assertThat(businessUnit1.getDeleted()).isFalse();

        BusinessTeam subBusinessTeam1 = getBusinessGroupItemByName(businessUnit1.getTeams(), "subBusinessGroupName1");
        assertThat(subBusinessTeam1).isNotNull();
        assertThat(subBusinessTeam1.getDeleted()).isFalse();

        BusinessUnit businessUnit2 = getBusinessGroupByName(businessUnitList,"businessGroupName2");
        assertThat(businessUnit2).isNotNull();
        assertThat(businessUnit2.getDeleted()).isFalse();

        BusinessTeam subBusinessTeam2 = getBusinessGroupItemByName(businessUnit2.getTeams(), "subBusinessGroupName2");
        assertThat(subBusinessTeam2).isNotNull();
        assertThat(subBusinessTeam2.getDeleted()).isFalse();

        BusinessTeam businessTeam3 = getBusinessGroupItemByName(businessUnit2.getTeams(), "subBusinessGroupName3");
        assertThat(businessTeam3).isNotNull();
        assertThat(businessTeam3.getDeleted()).isTrue();
    }

    @Test
    public void testServiceUpdateBusinessGroupFromCSVBoth() throws ListNotFoundException, GroupCreationException {
        Set<BusinessUnit> businessUnits = getBusinessGroups();
        when(mockUnitRepo.findAll()).thenReturn(businessUnits);

        CSVBusinessGroupLine lineOne = new CSVBusinessGroupLine("businessGroupName1", "businessGroupName1", "subBusinessGroupName1", "subBusinessGroupName1");
        CSVBusinessGroupLine lineThree = new CSVBusinessGroupLine("businessGroupName2", "businessGroupName2", "subBusinessGroupName3", "subBusinessGroupName3");
        CSVBusinessGroupLine lineFour = new CSVBusinessGroupLine("businessGroupName2", "businessGroupName4", "subBusinessGroupName4", "subBusinessGroupName4");
        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineThree);
        lines.add(lineFour);
        BusinessGroupService.updateBusinessUnits(lines);

        verify(mockUnitRepo, times(1)).save(anyList());
    }

    @Test
    public void testServiceUpdateBusinessGroupFromCSVSame() throws ListNotFoundException, GroupCreationException {
        Set<BusinessUnit> businessUnits = getBusinessGroups();
        when(mockUnitRepo.findAll()).thenReturn(businessUnits);

        CSVBusinessGroupLine lineOne = new CSVBusinessGroupLine("businessGroupName1", "businessGroupName1", "subBusinessGroupName1", "subBusinessGroupName1");
        CSVBusinessGroupLine lineTwo = new CSVBusinessGroupLine("businessGroupName2", "businessGroupName2", "subBusinessGroupName2", "subBusinessGroupName2");
        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineTwo);
        BusinessGroupService.updateBusinessUnits(lines);

        verify(mockUnitRepo, times(1)).save(anyList());
    }

    @Test
    public void testServiceUpdateBusinessGroupFromCSVNothingNone() throws ListNotFoundException, GroupCreationException {
        Set<BusinessUnit> businessUnits = getBusinessGroups();
        when(mockUnitRepo.findAll()).thenReturn(businessUnits);

        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        BusinessGroupService.updateBusinessUnits(lines);

        verify(mockUnitRepo, times(1)).save(anyList());
    }

    private static Set<BusinessUnit> getBusinessGroups() throws GroupCreationException {
        Set<BusinessUnit> businessUnits = new HashSet<>();

        BusinessTeam subBusinessTeamOne = new BusinessTeam("subBusinessGroupName1", "businessGroupName1_subBusinessGroupName1");
        Set<BusinessTeam> subBusinessTeams = new HashSet<>();
        subBusinessTeams.add(subBusinessTeamOne);
        BusinessUnit businessUnitOne = new BusinessUnit("businessGroupName1");
        businessUnitOne.setTeams(subBusinessTeams);

        BusinessTeam subBusinessTeamTwo = new BusinessTeam("subBusinessGroupName2","businessGroupName2_subBusinessGroupName2");
        BusinessTeam subBusinessTeamThree = new BusinessTeam("subBusinessGroupName3","businessGroupName3_subBusinessGroupName3");
        Set<BusinessTeam> subBusinessGroupsTwo = new HashSet<>();
        subBusinessGroupsTwo.add(subBusinessTeamTwo);
        subBusinessGroupsTwo.add(subBusinessTeamThree);
        BusinessUnit businessUnitTwo = new BusinessUnit("businessGroupName2");
        businessUnitTwo.setTeams(subBusinessGroupsTwo);

        businessUnits.add(businessUnitOne);
        businessUnits.add(businessUnitTwo);
        return businessUnits;
    }

    private static Set<BusinessUnit> getBusinessGroupsChildDeleted(Boolean parent, Boolean child) throws GroupCreationException {
        Set<BusinessUnit> businessTeams = new HashSet<>();

        BusinessTeam subBusinessTeamOne = new BusinessTeam("subBusinessGroupName1", "businessGroupName1_subBusinessGroupName1");
        subBusinessTeamOne.setDeleted(child);
        Set<BusinessTeam> subBusinessTeams = new HashSet<>();
        subBusinessTeams.add(subBusinessTeamOne);
        BusinessUnit businessUnitOne = new BusinessUnit("businessGroupName1");
        businessUnitOne.setTeams(subBusinessTeams);
        businessUnitOne.setDeleted(parent);

        businessTeams.add(businessUnitOne);
        return businessTeams;
    }

    private static Set<CSVBusinessGroupLine> buildValidCSVBusinessGroupLines() {
        Set<CSVBusinessGroupLine> lines = new HashSet<>();

        CSVBusinessGroupLine line = new CSVBusinessGroupLine("ParentbusinessGroupName", "businessGroupName", "BusinessGroupUnit", "BusinessGroupTeam");
        lines.add(line);

        return lines;
    }

    private static Set<BusinessUnit> buildbusinessGroupList() throws GroupCreationException {
        BusinessUnit businessUnit = new BusinessUnit("businessGroupName" );

        Set<BusinessTeam> businessTeams = new HashSet<>();
        businessTeams.add(new BusinessTeam("subBusinessGroupName"));
        businessUnit.setTeams(businessTeams);

        Set<BusinessUnit> records = new HashSet<>();
        records.add(businessUnit);

        return records;
    }

    private static BusinessUnit getBusinessGroupByName(Set<BusinessUnit> businessUnits, String businessGroupName)
    {
        return businessUnits.stream().filter(t -> t.getDisplayName().equals(businessGroupName)).findFirst().orElse(null);
    }

    private static BusinessTeam getBusinessGroupItemByName(Set<BusinessTeam> businessTeam, String businessGroupName)
    {
        return businessTeam.stream().filter(t -> t.getDisplayName().equals(businessGroupName)).findFirst().orElseGet(null);
    }


}