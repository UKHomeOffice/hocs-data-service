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
import uk.gov.digital.ho.hocs.model.BusinessGroup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BusinessGroupServiceTest {

    private final static String CASETYPE = "Test";
    private final static String UNAVAILABLE_RESOURCE = "Unavailable Resource";

    @Mock
    private BusinessGroupRepository mockRepo;

    @Captor
    private ArgumentCaptor<HashSet<BusinessGroup>> captor;

    private BusinessGroupService BusinessGroupService;


    @Before
    public void setUp() {
        BusinessGroupService = new BusinessGroupService(mockRepo);
    }

    @Test
    public void testCollaboratorsGettingBusinessGroup() throws ListNotFoundException, GroupCreationException {
        BusinessGroup businessGroupOne = new BusinessGroup("businessGroupName");
        when(mockRepo.findOneByReferenceNameAndDeletedIsFalse(CASETYPE)).thenReturn(businessGroupOne);

        BusinessGroup record = BusinessGroupService.getGroupByReference(CASETYPE);

        verify(mockRepo).findOneByReferenceNameAndDeletedIsFalse(CASETYPE);

        assertThat(record).isNotNull();
        assertThat(record.getDisplayName()).isEqualTo("businessGroupName");
    }

    @Test(expected = ListNotFoundException.class)
    public void testLegacyListNotFoundThrowsListNotFoundException() throws ListNotFoundException {

        BusinessGroupService.getGroupByReference(UNAVAILABLE_RESOURCE);
        verify(mockRepo).findAllByDeletedIsFalse();
    }

    @Test
    public void testCollaboratorsGettingAllBusinessGroup() throws ListNotFoundException, GroupCreationException {
        when(mockRepo.findAllByDeletedIsFalse()).thenReturn(buildbusinessGroupList());

        List<BusinessGroup> records = BusinessGroupService.getAllBusinessGroups().stream().collect(Collectors.toList());

        verify(mockRepo).findAllByDeletedIsFalse();

        assertThat(records).isNotNull();
        assertThat(records).hasOnlyElementsOfType(BusinessGroup.class);
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getDisplayName()).isEqualTo("businessGroupName");
    }

    @Test(expected = ListNotFoundException.class)
    public void testAllListNotFoundThrowsListNotFoundException() throws ListNotFoundException {

        List<BusinessGroup> records = BusinessGroupService.getAllBusinessGroups().stream().collect(Collectors.toList());
        verify(mockRepo).findAllByDeletedIsFalse();
        assertThat(records).isEmpty();
    }

    @Test
    public void testCreateList() {
        BusinessGroupService.updateBusinessGroups(buildValidCSVBusinessGroupLines());
        verify(mockRepo).save(anyList());
    }

    @Test
    public void testCreateListNoEntities() {
        BusinessGroupService.updateBusinessGroups(new HashSet<>());
        verify(mockRepo, times(0)).save(anyList());
    }

    @Test(expected = EntityCreationException.class)
    public void testRepoDataIntegrityExceptionThrowsEntityCreationException() {

        Set<CSVBusinessGroupLine> BusinessGroup = buildValidCSVBusinessGroupLines();

        when(mockRepo.save(anyList())).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "group_name_ref_idempotent")));
        BusinessGroupService.updateBusinessGroups(BusinessGroup);

        verify(mockRepo).save(anyList());
    }

    @Test(expected = EntityCreationException.class)
    public void testRepoDataIntegrityExceptionThrowsEntityCreationExceptionTwo() {

        Set<CSVBusinessGroupLine> BusinessGroup = buildValidCSVBusinessGroupLines();

        when(mockRepo.save(anyList())).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "group_name_ref_idempotent")));
        BusinessGroupService.updateBusinessGroups(BusinessGroup);

        verify(mockRepo).save(anyList());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testRepoDataIntegrityExceptionThrowsDataIntegrityViolationException() {

        Set<CSVBusinessGroupLine> BusinessGroup = buildValidCSVBusinessGroupLines();

        when(mockRepo.save(anyList())).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "")));
        BusinessGroupService.updateBusinessGroups(BusinessGroup);

        verify(mockRepo).save(anyList());
    }

    @Test
    public void testServiceUpdateBusinessGroupFromCSVAdd() throws ListNotFoundException, GroupCreationException {
        Set<BusinessGroup> businessGroups = getBusinessGroups();
        when(mockRepo.findAll()).thenReturn(businessGroups);

        CSVBusinessGroupLine lineOne = new CSVBusinessGroupLine("businessGroupName1", "businessGroupName1", "subBusinessGroupName1", "subBusinessGroupName1");
        CSVBusinessGroupLine lineTwo = new CSVBusinessGroupLine("businessGroupName2", "businessGroupName2", "subBusinessGroupName2", "subBusinessGroupName2");
        CSVBusinessGroupLine lineThree = new CSVBusinessGroupLine("businessGroupName3", "businessGroupName3", "subBusinessGroupName3", "subBusinessGroupName3");
        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineTwo);
        lines.add(lineThree);
        BusinessGroupService.updateBusinessGroups(lines);

        verify(mockRepo, times(1)).save(anyList());
    }

    @Test
    public void testServiceUpdateBusinessGroupFromCsvAddWhenAlreadyDeleted() throws GroupCreationException {
        Set<BusinessGroup> businessGroups = getBusinessGroupsChildDeleted(true, true);
        when(mockRepo.findAll()).thenReturn(businessGroups);

        CSVBusinessGroupLine lineOne = new CSVBusinessGroupLine("businessGroupName1", "businessGroupName1", "subBusinessGroupName1", "subBusinessGroupName1");
        CSVBusinessGroupLine lineTwo = new CSVBusinessGroupLine("businessGroupName2", "businessGroupName2", "subBusinessGroupName2", "subBusinessGroupName2");
        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineTwo);
        BusinessGroupService.updateBusinessGroups(lines);

        verify(mockRepo).save(captor.capture());
        final Set<BusinessGroup> businessGroupList = captor.getValue();

        verify(mockRepo, times(1)).save(anyList());
        assertThat(businessGroupList).isNotNull();
        assertThat(businessGroupList).hasSize(2);

        BusinessGroup businessGroup1 = getBusinessGroupByName(businessGroupList,"businessGroupName1");
        assertThat(businessGroup1).isNotNull();
        assertThat(businessGroup1.getDeleted()).isFalse();

        BusinessGroup subBusinessGroup1 = getBusinessGroupItemByName(businessGroup1.getSubGroups(), "subBusinessGroupName1");
        assertThat(subBusinessGroup1).isNotNull();
        assertThat(subBusinessGroup1.getDeleted()).isFalse();

        BusinessGroup businessGroup2 = getBusinessGroupByName(businessGroupList,"businessGroupName2");
        assertThat(businessGroup2).isNotNull();
        assertThat(businessGroup2.getDeleted()).isFalse();

        BusinessGroup subBusinessGroup2 = getBusinessGroupItemByName(businessGroup2.getSubGroups(), "subBusinessGroupName2");
        assertThat(subBusinessGroup2).isNotNull();
        assertThat(subBusinessGroup2.getDeleted()).isFalse();
    }

    @Test
    public void testServiceUpdateBusinessGroupFromCsvEmptyGroupIsDeleted() throws GroupCreationException {
        Set<BusinessGroup> businessGroups = getBusinessGroupsChildDeleted(true, true);
        when(mockRepo.findAll()).thenReturn(businessGroups);

        CSVBusinessGroupLine lineOne = new CSVBusinessGroupLine("businessGroupName1", "businessGroupName1", "subBusinessGroupName4", "subBusinessGroupName4");
        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        lines.add(lineOne);
        BusinessGroupService.updateBusinessGroups(lines);

        verify(mockRepo).save(captor.capture());
        final Set<BusinessGroup> businessGroupList = captor.getValue();

        verify(mockRepo, times(1)).save(anyList());
        assertThat(businessGroupList).isNotNull();
        assertThat(businessGroupList).hasSize(1);

        BusinessGroup businessGroup1 = getBusinessGroupByName(businessGroupList,"businessGroupName1");
        assertThat(businessGroup1).isNotNull();
        assertThat(businessGroup1.getDeleted()).isFalse();

        BusinessGroup subBusinessGroup1 = getBusinessGroupItemByName(businessGroup1.getSubGroups(), "subBusinessGroupName1");
        assertThat(subBusinessGroup1).isNotNull();
        assertThat(subBusinessGroup1.getDeleted()).isTrue();

        BusinessGroup businessGroup2 = getBusinessGroupItemByName(businessGroup1.getSubGroups(), "subBusinessGroupName4");
        assertThat(businessGroup2).isNotNull();
        assertThat(businessGroup2.getDeleted()).isFalse();
    }

    @Test
    public void testServiceUpdateBusinessGroupFromCSVRemove() throws ListNotFoundException, GroupCreationException {
        Set<BusinessGroup> businessGroups = getBusinessGroups();
        when(mockRepo.findAll()).thenReturn(businessGroups);

        CSVBusinessGroupLine lineOne = new CSVBusinessGroupLine("businessGroupName1", "businessGroupName1", "subBusinessGroupName1", "subBusinessGroupName1");
        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        lines.add(lineOne);
        BusinessGroupService.updateBusinessGroups(lines);

        verify(mockRepo).save(captor.capture());
        final Set<BusinessGroup> businessGroupList = captor.getValue();

        verify(mockRepo, times(1)).save(anyList());
        assertThat(businessGroupList).isNotNull();
        assertThat(businessGroupList).hasSize(2);

        BusinessGroup businessGroup1 = getBusinessGroupByName(businessGroupList,"businessGroupName1");
        assertThat(businessGroup1).isNotNull();
        assertThat(businessGroup1.getDeleted()).isFalse();

        BusinessGroup subBusinessGroup1 = getBusinessGroupItemByName(businessGroup1.getSubGroups(), "subBusinessGroupName1");
        assertThat(subBusinessGroup1).isNotNull();
        assertThat(subBusinessGroup1.getDeleted()).isFalse();

        BusinessGroup businessGroup2 = getBusinessGroupByName(businessGroupList,"businessGroupName2");
        assertThat(businessGroup2).isNotNull();
        assertThat(businessGroup2.getDeleted()).isTrue();

        BusinessGroup subBusinessGroup2 = getBusinessGroupItemByName(businessGroup2.getSubGroups(), "subBusinessGroupName2");
        assertThat(subBusinessGroup2).isNotNull();
        assertThat(subBusinessGroup2.getDeleted()).isTrue();

    }

    @Test
    public void testServiceUpdateBusinessGroupFromCSVRemoveChildBusinessGroup() throws ListNotFoundException, GroupCreationException {
        Set<BusinessGroup> businessGroups = getBusinessGroups();
        when(mockRepo.findAll()).thenReturn(businessGroups);

        CSVBusinessGroupLine lineOne = new CSVBusinessGroupLine("businessGroupName1", "businessGroupName1", "subBusinessGroupName1", "subBusinessGroupName1");
        CSVBusinessGroupLine lineTwo = new CSVBusinessGroupLine("businessGroupName2", "businessGroupName2", "subBusinessGroupName2", "subBusinessGroupName2");
        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineTwo);
        BusinessGroupService.updateBusinessGroups(lines);

        verify(mockRepo).save(captor.capture());
        final Set<BusinessGroup> businessGroupList = captor.getValue();

        verify(mockRepo, times(1)).save(anyList());
        assertThat(businessGroupList).isNotNull();
        assertThat(businessGroupList).hasSize(2);

        BusinessGroup businessGroup1 = getBusinessGroupByName(businessGroupList,"businessGroupName1");
        assertThat(businessGroup1).isNotNull();
        assertThat(businessGroup1.getDeleted()).isFalse();

        BusinessGroup subBusinessGroup1 = getBusinessGroupItemByName(businessGroup1.getSubGroups(), "subBusinessGroupName1");
        assertThat(subBusinessGroup1).isNotNull();
        assertThat(subBusinessGroup1.getDeleted()).isFalse();

        BusinessGroup businessGroup2 = getBusinessGroupByName(businessGroupList,"businessGroupName2");
        assertThat(businessGroup2).isNotNull();
        assertThat(businessGroup2.getDeleted()).isFalse();

        BusinessGroup subBusinessGroup2 = getBusinessGroupItemByName(businessGroup2.getSubGroups(), "subBusinessGroupName2");
        assertThat(subBusinessGroup2).isNotNull();
        assertThat(subBusinessGroup2.getDeleted()).isFalse();

        BusinessGroup businessGroup3 = getBusinessGroupItemByName(businessGroup2.getSubGroups(), "subBusinessGroupName3");
        assertThat(businessGroup3).isNotNull();
        assertThat(businessGroup3.getDeleted()).isTrue();
    }

    @Test
    public void testServiceUpdateBusinessGroupFromCSVBoth() throws ListNotFoundException, GroupCreationException {
        Set<BusinessGroup> businessGroups = getBusinessGroups();
        when(mockRepo.findAll()).thenReturn(businessGroups);

        CSVBusinessGroupLine lineOne = new CSVBusinessGroupLine("businessGroupName1", "businessGroupName1", "subBusinessGroupName1", "subBusinessGroupName1");
        CSVBusinessGroupLine lineThree = new CSVBusinessGroupLine("businessGroupName2", "businessGroupName2", "subBusinessGroupName3", "subBusinessGroupName3");
        CSVBusinessGroupLine lineFour = new CSVBusinessGroupLine("businessGroupName2", "businessGroupName4", "subBusinessGroupName4", "subBusinessGroupName4");
        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineThree);
        lines.add(lineFour);
        BusinessGroupService.updateBusinessGroups(lines);

        verify(mockRepo, times(1)).save(anyList());
    }

    @Test
    public void testServiceUpdateBusinessGroupFromCSVSame() throws ListNotFoundException, GroupCreationException {
        Set<BusinessGroup> businessGroups = getBusinessGroups();
        when(mockRepo.findAll()).thenReturn(businessGroups);

        CSVBusinessGroupLine lineOne = new CSVBusinessGroupLine("businessGroupName1", "businessGroupName1", "subBusinessGroupName1", "subBusinessGroupName1");
        CSVBusinessGroupLine lineTwo = new CSVBusinessGroupLine("businessGroupName2", "businessGroupName2", "subBusinessGroupName2", "subBusinessGroupName2");
        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineTwo);
        BusinessGroupService.updateBusinessGroups(lines);

        verify(mockRepo, times(1)).save(anyList());
    }

    @Test
    public void testServiceUpdateBusinessGroupFromCSVNothingNone() throws ListNotFoundException, GroupCreationException {
        Set<BusinessGroup> businessGroups = getBusinessGroups();
        when(mockRepo.findAll()).thenReturn(businessGroups);

        Set<CSVBusinessGroupLine> lines = new HashSet<>();
        BusinessGroupService.updateBusinessGroups(lines);

        verify(mockRepo, times(1)).save(anyList());
    }

    private static Set<BusinessGroup> getBusinessGroups() throws GroupCreationException {
        Set<BusinessGroup> businessGroup = new HashSet<>();

        BusinessGroup subBusinessGroupOne = new BusinessGroup("subBusinessGroupName1", "businessGroupName1_subBusinessGroupName1");
        Set<BusinessGroup> subBusinessGroups = new HashSet<>();
        subBusinessGroups.add(subBusinessGroupOne);
        BusinessGroup businessGroupOne = new BusinessGroup("businessGroupName1");
        businessGroupOne.setSubGroups(subBusinessGroups);

        BusinessGroup subBusinessGroupTwo = new BusinessGroup("subBusinessGroupName2","businessGroupName2_subBusinessGroupName2");
        BusinessGroup subBusinessGroupThree = new BusinessGroup("subBusinessGroupName3","businessGroupName3_subBusinessGroupName3");
        Set<BusinessGroup> subBusinessGroupsTwo = new HashSet<>();
        subBusinessGroupsTwo.add(subBusinessGroupTwo);
        subBusinessGroupsTwo.add(subBusinessGroupThree);
        BusinessGroup businessGroupTwo = new BusinessGroup("businessGroupName2");
        businessGroupTwo.setSubGroups(subBusinessGroupsTwo);

        businessGroup.add(businessGroupOne);
        businessGroup.add(businessGroupTwo);
        return businessGroup;
    }

    private static Set<BusinessGroup> getBusinessGroupsChildDeleted(Boolean parent, Boolean child) throws GroupCreationException {
        Set<BusinessGroup> businessGroups = new HashSet<>();

        BusinessGroup subBusinessGroupOne = new BusinessGroup("subBusinessGroupName1", "businessGroupName1_subBusinessGroupName1");
        subBusinessGroupOne.setDeleted(child);
        Set<BusinessGroup> subBusinessGroups = new HashSet<>();
        subBusinessGroups.add(subBusinessGroupOne);
        BusinessGroup businessGroupOne = new BusinessGroup("businessGroupName1");
        businessGroupOne.setSubGroups(subBusinessGroups);
        businessGroupOne.setDeleted(parent);

        businessGroups.add(businessGroupOne);
        return businessGroups;
    }

    private static Set<CSVBusinessGroupLine> buildValidCSVBusinessGroupLines() {
        Set<CSVBusinessGroupLine> lines = new HashSet<>();

        CSVBusinessGroupLine line = new CSVBusinessGroupLine("ParentbusinessGroupName", "businessGroupName", "BusinessGroupUnit", "BusinessGroupTeam");
        lines.add(line);

        return lines;
    }

    private static Set<BusinessGroup> buildbusinessGroupList() throws GroupCreationException {
        BusinessGroup businessGroup = new BusinessGroup("businessGroupName" );

        Set<BusinessGroup> businessGroups = new HashSet<>();
        businessGroups.add(new BusinessGroup("subBusinessGroupName"));
        businessGroup.setSubGroups(businessGroups);

        Set<BusinessGroup> records = new HashSet<>();
        records.add(businessGroup);

        return records;
    }

    private static BusinessGroup getBusinessGroupByName(Set<BusinessGroup> businessGroups, String businessGroupName)
    {
        return businessGroups.stream().filter(t -> t.getDisplayName().equals(businessGroupName)).findFirst().orElse(null);
    }

    private static BusinessGroup getBusinessGroupItemByName(Set<BusinessGroup> businessGroup, String businessGroupName)
    {
        return businessGroup.stream().filter(t -> t.getDisplayName().equals(businessGroupName)).findFirst().orElseGet(null);
    }


}