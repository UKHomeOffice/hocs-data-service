package uk.gov.digital.ho.hocs.businessGroups;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;
import uk.gov.digital.ho.hocs.AlfrescoClient;
import uk.gov.digital.ho.hocs.businessGroups.ingest.CSVBusinessGroupLine;
import uk.gov.digital.ho.hocs.businessGroups.model.BusinessTeam;
import uk.gov.digital.ho.hocs.businessGroups.model.BusinessUnit;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.EntityNotFoundException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Mock
    private AlfrescoClient alfrescoClient;

    @Captor
    private ArgumentCaptor<HashSet<BusinessUnit>> captor;

    private uk.gov.digital.ho.hocs.businessGroups.BusinessUnitService BusinessUnitService;


    @Before
    public void setUp() {
        BusinessUnitService = new BusinessUnitService(mockUnitRepo, mockTeamRepo, alfrescoClient);
    }

    @Test
    public void testCollaboratorsGettingBusinessGroup() throws EntityNotFoundException, EntityCreationException {
        BusinessTeam businessTeamOne = new BusinessTeam("businessGroupName");
        when(mockTeamRepo.findOneByReferenceNameAndDeletedIsFalse(CASETYPE)).thenReturn(businessTeamOne);

        BusinessTeam record = new ArrayList<>(BusinessUnitService.getTeamByReference(CASETYPE)).get(0);

        verify(mockTeamRepo).findOneByReferenceNameAndDeletedIsFalse(CASETYPE);

        assertThat(record).isNotNull();
        assertThat(record.getDisplayName()).isEqualTo("businessGroupName");
    }

    @Test(expected = EntityNotFoundException.class)
    public void testLegacyListNotFoundThrowsListNotFoundException() throws EntityNotFoundException {

        BusinessUnitService.getTeamByReference(UNAVAILABLE_RESOURCE);
        verify(mockUnitRepo).findAllByDeletedIsFalse();
    }

    @Test
    public void testCollaboratorsGettingAllBusinessGroup() throws EntityNotFoundException, EntityCreationException {
        when(mockUnitRepo.findAllByDeletedIsFalse()).thenReturn(buildbusinessGroupList());

        List<BusinessUnit> records = new ArrayList<>(BusinessUnitService.getAllBusinessUnits());

        verify(mockUnitRepo).findAllByDeletedIsFalse();

        assertThat(records).isNotNull();
        assertThat(records).hasOnlyElementsOfType(BusinessUnit.class);
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getDisplayName()).isEqualTo("businessGroupName");
    }

    @Test
    public void testCreateList() throws EntityCreationException {
        BusinessUnitService.updateBusinessUnits(buildValidCSVBusinessGroupLines());
        verify(mockUnitRepo).saveAll(any());
    }

    @Test(expected = EntityCreationException.class)
    public void testCreateLisTooLong() throws EntityCreationException {
        Set<CSVBusinessGroupLine> lines = new HashSet<>();

        CSVBusinessGroupLine line = new CSVBusinessGroupLine("ParentbusinessGroupNametoolong", "ParentbusinessGroupNametooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooolong", "BusinessGroupUnit", "BusinessGroupTeam");
        lines.add(line);

        BusinessUnitService.updateBusinessUnits(lines);
        verify(mockUnitRepo, times(0)).saveAll(anyList());
    }

    @Test(expected = EntityCreationException.class)
    public void testCreateListNull() throws EntityCreationException {
        BusinessUnitService.updateBusinessUnits(null);
        verify(mockUnitRepo, times(0)).saveAll(anyList());
    }

    @Test
    public void testCreateListNoEntities() throws EntityCreationException {
        BusinessUnitService.updateBusinessUnits(new HashSet<>());
        verify(mockUnitRepo, times(0)).saveAll(anyList());
    }

    @Test(expected = EntityCreationException.class)
    public void testRepoDataIntegrityExceptionThrowsEntityCreationException() throws EntityCreationException {

        Set<CSVBusinessGroupLine> BusinessGroup = buildValidCSVBusinessGroupLines();

        when(mockUnitRepo.saveAll(any())).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "group_name_ref_idempotent")));
        BusinessUnitService.updateBusinessUnits(BusinessGroup);

        verify(mockUnitRepo).saveAll(any());
    }

    @Test(expected = EntityCreationException.class)
    public void testRepoDataIntegrityExceptionThrowsEntityCreationExceptionTwo() throws EntityCreationException {

        Set<CSVBusinessGroupLine> BusinessGroup = buildValidCSVBusinessGroupLines();

        when(mockUnitRepo.saveAll(any())).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "group_name_ref_idempotent")));
        BusinessUnitService.updateBusinessUnits(BusinessGroup);

        verify(mockUnitRepo).saveAll(any());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testRepoDataIntegrityExceptionThrowsDataIntegrityViolationException() throws EntityCreationException {

        Set<CSVBusinessGroupLine> BusinessGroup = buildValidCSVBusinessGroupLines();

        when(mockUnitRepo.saveAll(any())).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "")));
        BusinessUnitService.updateBusinessUnits(BusinessGroup);

        verify(mockUnitRepo).saveAll(any());
    }

    @Test
    public void testServiceUpdateBusinessGroupFromCSVAdd() throws EntityNotFoundException, EntityCreationException {
        Set<BusinessUnit> businessUnits = getBusinessGroups();
        when(mockUnitRepo.findAll()).thenReturn(businessUnits);

        CSVBusinessGroupLine lineOne = new CSVBusinessGroupLine("businessGroupName1", "businessGroupName1", "subBusinessGroupName1", "subBusinessGroupName1");
        CSVBusinessGroupLine lineTwo = new CSVBusinessGroupLine("businessGroupName2", "businessGroupName2", "subBusinessGroupName2", "subBusinessGroupName2");
        CSVBusinessGroupLine lineThree = new CSVBusinessGroupLine("businessGroupName3", "businessGroupName3", "subBusinessGroupName3", "subBusinessGroupName3");
        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineTwo);
        lines.add(lineThree);
        BusinessUnitService.updateBusinessUnits(lines);

        verify(mockUnitRepo, times(1)).saveAll(any());
    }

    @Test
    public void testServiceUpdateBusinessGroupFromCsvAddWhenAlreadyDeleted() throws EntityCreationException {
        Set<BusinessUnit> businessUnits = getBusinessGroupsChildDeleted(true, true);
        when(mockUnitRepo.findAll()).thenReturn(businessUnits);

        CSVBusinessGroupLine lineOne = new CSVBusinessGroupLine("businessGroupName1", "businessGroupName1", "subBusinessGroupName1", "subBusinessGroupName1");
        CSVBusinessGroupLine lineTwo = new CSVBusinessGroupLine("businessGroupName2", "businessGroupName2", "subBusinessGroupName2", "subBusinessGroupName2");
        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineTwo);
        BusinessUnitService.updateBusinessUnits(lines);

        verify(mockUnitRepo).saveAll(captor.capture());
        final Set<BusinessUnit> businessUnitList = captor.getValue();

        verify(mockUnitRepo, times(1)).saveAll(any());
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
    public void testServiceUpdateBusinessGroupFromCsvEmptyGroupIsDeleted() throws EntityCreationException {
        Set<BusinessUnit> businessUnits = getBusinessGroupsChildDeleted(true, true);
        when(mockUnitRepo.findAll()).thenReturn(businessUnits);

        CSVBusinessGroupLine lineOne = new CSVBusinessGroupLine("businessGroupName1", "businessGroupName1", "subBusinessGroupName4", "subBusinessGroupName4");
        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        lines.add(lineOne);
        BusinessUnitService.updateBusinessUnits(lines);

        verify(mockUnitRepo).saveAll(captor.capture());
        final Set<BusinessUnit> businessUnitList = captor.getValue();

        verify(mockUnitRepo, times(1)).saveAll(any());
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
    public void testServiceUpdateBusinessGroupFromCSVRemove() throws EntityNotFoundException, EntityCreationException {
        Set<BusinessUnit> businessUnits = getBusinessGroups();
        when(mockUnitRepo.findAll()).thenReturn(businessUnits);

        CSVBusinessGroupLine lineOne = new CSVBusinessGroupLine("businessGroupName1", "businessGroupName1", "subBusinessGroupName1", "subBusinessGroupName1");
        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        lines.add(lineOne);
        BusinessUnitService.updateBusinessUnits(lines);

        verify(mockUnitRepo).saveAll(captor.capture());
        final Set<BusinessUnit> businessUnitList = captor.getValue();

        verify(mockUnitRepo, times(1)).saveAll(any());
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
    public void testServiceUpdateBusinessGroupFromCSVRemoveChildBusinessGroup() throws EntityNotFoundException, EntityCreationException {
        Set<BusinessUnit> businessUnits = getBusinessGroups();
        when(mockUnitRepo.findAll()).thenReturn(businessUnits);

        CSVBusinessGroupLine lineOne = new CSVBusinessGroupLine("businessGroupName1", "businessGroupName1", "subBusinessGroupName1", "subBusinessGroupName1");
        CSVBusinessGroupLine lineTwo = new CSVBusinessGroupLine("businessGroupName2", "businessGroupName2", "subBusinessGroupName2", "subBusinessGroupName2");
        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineTwo);
        BusinessUnitService.updateBusinessUnits(lines);

        verify(mockUnitRepo).saveAll(captor.capture());
        final Set<BusinessUnit> businessUnitList = captor.getValue();

        verify(mockUnitRepo, times(1)).saveAll(any());
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
    public void testServiceUpdateBusinessGroupFromCSVBoth() throws EntityNotFoundException, EntityCreationException {
        Set<BusinessUnit> businessUnits = getBusinessGroups();
        when(mockUnitRepo.findAll()).thenReturn(businessUnits);

        CSVBusinessGroupLine lineOne = new CSVBusinessGroupLine("businessGroupName1", "businessGroupName1", "subBusinessGroupName1", "subBusinessGroupName1");
        CSVBusinessGroupLine lineThree = new CSVBusinessGroupLine("businessGroupName2", "businessGroupName2", "subBusinessGroupName3", "subBusinessGroupName3");
        CSVBusinessGroupLine lineFour = new CSVBusinessGroupLine("businessGroupName2", "businessGroupName4", "subBusinessGroupName4", "subBusinessGroupName4");
        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineThree);
        lines.add(lineFour);
        BusinessUnitService.updateBusinessUnits(lines);

        verify(mockUnitRepo, times(1)).saveAll(any());
    }

    @Test
    public void testServiceUpdateBusinessGroupFromCSVSame() throws EntityNotFoundException, EntityCreationException {
        Set<BusinessUnit> businessUnits = getBusinessGroups();
        when(mockUnitRepo.findAll()).thenReturn(businessUnits);

        CSVBusinessGroupLine lineOne = new CSVBusinessGroupLine("businessGroupName1", "businessGroupName1", "subBusinessGroupName1", "subBusinessGroupName1");
        CSVBusinessGroupLine lineTwo = new CSVBusinessGroupLine("businessGroupName2", "businessGroupName2", "subBusinessGroupName2", "subBusinessGroupName2");
        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineTwo);
        BusinessUnitService.updateBusinessUnits(lines);

        verify(mockUnitRepo, times(1)).saveAll(any());
    }

    @Test
    public void testServiceUpdateBusinessGroupFromCSVNothingNone() throws EntityNotFoundException, EntityCreationException {
        Set<BusinessUnit> businessUnits = getBusinessGroups();
        when(mockUnitRepo.findAll()).thenReturn(businessUnits);

        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        BusinessUnitService.updateBusinessUnits(lines);

        verify(mockUnitRepo, times(1)).saveAll(any());
    }

    private static Set<BusinessUnit> getBusinessGroups() throws EntityCreationException {
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

    private static Set<BusinessUnit> getBusinessGroupsChildDeleted(Boolean parent, Boolean child) throws EntityCreationException {
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

    private static Set<BusinessUnit> buildbusinessGroupList() throws EntityCreationException {
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