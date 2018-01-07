package uk.gov.digital.ho.hocs.lists;

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
import uk.gov.digital.ho.hocs.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.lists.dto.DataListEntityRecord;
import uk.gov.digital.ho.hocs.lists.dto.DataListRecord;
import uk.gov.digital.ho.hocs.lists.model.DataList;
import uk.gov.digital.ho.hocs.lists.model.DataListEntity;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DataListServiceTest {

    private final static String TEST_LIST = "Test List One";
    private final static String UNAVAILABLE_RESOURCE = "Unavailable Resource";

    @Mock
    private DataListRepository mockRepo;

    @Captor
    private ArgumentCaptor<DataList> captor;

    private DataListService service;

    @Before
    public void setUp() {
        service = new DataListService(mockRepo);
    }

    @Test
    public void testCollaboratorsGettingDataLists() throws EntityNotFoundException {
        when(mockRepo.findAllByDeletedIsFalse()).thenReturn(getDataLists());

        List<DataList> records = new ArrayList<>(service.getAllDataLists());

        verify(mockRepo).findAllByDeletedIsFalse();

        assertThat(records).isNotNull();
        assertThat(records).hasOnlyElementsOfType(DataList.class);
        assertThat(records).hasSize(2);

        assertThat(records.get(1).getName()).isEqualTo(TEST_LIST+"2");
        assertThat(records.get(1).getEntities()).hasSize(2);

        assertThat(records.get(0).getName()).isEqualTo(TEST_LIST);
        assertThat(records.get(0).getEntities()).hasSize(1);
    }

    @Test
    public void testCollaboratorsGettingDataList() throws EntityNotFoundException {
        when(mockRepo.findOneByNameAndDeletedIsFalse(TEST_LIST)).thenReturn(getDataList());

        DataList record = service.getDataListByName(TEST_LIST);

        verify(mockRepo).findOneByNameAndDeletedIsFalse(TEST_LIST);

        assertThat(record).isNotNull();
        assertThat(record.getName()).isEqualTo(TEST_LIST);
        assertThat(record.getEntities()).hasSize(1);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testAllListNotFoundThrowsListNotFoundException() throws EntityNotFoundException {

        DataList record = service.getDataListByName(UNAVAILABLE_RESOURCE);
        verify(mockRepo).findOneByNameAndDeletedIsFalse(UNAVAILABLE_RESOURCE);
        assertThat(record).isNull();
    }

    @Test
    public void testCreateList() {
        service.updateDataList(getDataList());
        verify(mockRepo).save(any(DataList.class));
    }


    @Test(expected = EntityCreationException.class)
    public void testCreateListNull() {
        service.updateDataList(null);
        verify(mockRepo, times(0)).save(anyList());
    }

    @Test
    public void testCreateListNoEntities() {
        service.updateDataList(new DataList(new DataListRecord(TEST_LIST, new ArrayList<>())));
        verify(mockRepo, times(0)).save(anyList());
    }

    @Test(expected = EntityCreationException.class)
    public void testRepoDataIntegrityExceptionThrowsEntityCreationException() {

        DataList datalist1 = getDataList();

        when(mockRepo.save(datalist1)).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "DataList_name_idempotent")));
        service.updateDataList(datalist1);

        verify(mockRepo).save(datalist1);
    }

    @Test(expected = EntityCreationException.class)
    public void testRepoDataIntegrityExceptionThrowsEntityCreationExceptionTwo() {

        DataList datalist1 = getDataList();

        when(mockRepo.save(datalist1)).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "dataListEntity_name_ref_idempotent")));
        service.updateDataList(datalist1);

        verify(mockRepo).save(datalist1);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testRepoDataIntegrityExceptionThrowsDataIntegrityViolationException() {

        DataList datalist1 = getDataList();

        when(mockRepo.save(datalist1)).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "")));
        service.updateDataList(datalist1);

        verify(mockRepo).save(datalist1);
    }

    @Test
    public void testServiceUpdateDataListAdd() throws EntityNotFoundException {
        DataList datalist1 = getDataList();

        when(mockRepo.findOneByName(TEST_LIST)).thenReturn(datalist1);

        DataListEntityRecord dataListEntity1 = new DataListEntityRecord("Text1", "Text1");
        DataListEntityRecord dataListEntity2 = new DataListEntityRecord("Text2", "Text2");
        DataListEntityRecord dataListEntity3 = new DataListEntityRecord("Text3", "Text3");
        List<DataListEntityRecord> datalistEntitys = new ArrayList<>();
        datalistEntitys.addAll(Arrays.asList(dataListEntity1, dataListEntity2, dataListEntity3));
        DataList datalist = new DataList(new DataListRecord(TEST_LIST, datalistEntitys));
        service.updateDataList(datalist);

        verify(mockRepo, times(1)).save(datalist);
    }

    @Test
    public void testServiceUpdateDataListAddWhenAlreadyDeleted() {
        DataList datalist1 = getDataListChildDeleted(true, true);
        when(mockRepo.findOneByName(TEST_LIST)).thenReturn(datalist1);

        DataListEntityRecord datalistEntity1 = new DataListEntityRecord("Text1", "Text1");
        DataListEntityRecord datalistEntity2 = new DataListEntityRecord("Text2", "Text2");
        List<DataListEntityRecord> datalistEntitys = new ArrayList<>();
        datalistEntitys.add(datalistEntity1);
        datalistEntitys.add(datalistEntity2);
        DataList newDataList = new DataList(new DataListRecord(TEST_LIST, datalistEntitys));
        service.updateDataList(newDataList);

        verify(mockRepo).save(captor.capture());
        final DataList datalist = captor.getValue();

        verify(mockRepo, times(1)).save(any(DataList.class));
        assertThat(datalist).isNotNull();
        assertThat(datalist.getEntities()).hasSize(2);
        assertThat(datalist.getDeleted()).isFalse();

        DataListEntity person1 = getDataListEntityByName(datalist,"Text1");
        assertThat(person1).isNotNull();
        assertThat(person1.getDeleted()).isFalse();


        DataListEntity person2 = getDataListEntityByName(datalist,"Text2");
        assertThat(person2).isNotNull();
        assertThat(person2.getDeleted()).isFalse();

    }

    @Test
    public void testServiceUpdateDataListEmptyGroupIsDeleted() {
        DataList datalist1 = getDataListChildDeleted(true, true);
        when(mockRepo.findOneByName(TEST_LIST)).thenReturn(datalist1);

        List<DataListEntityRecord> dataListEntitys = new ArrayList<>();
        dataListEntitys.add(new DataListEntityRecord("Text4",  "Text4"));
        DataList newDataList = new DataList(new DataListRecord(TEST_LIST, dataListEntitys));
        service.updateDataList(newDataList);

        verify(mockRepo).save(captor.capture());
        final DataList datalist = captor.getValue();

        verify(mockRepo, times(1)).save(any(DataList.class));
        assertThat(datalist).isNotNull();
        assertThat(datalist.getEntities()).hasSize(2);
        assertThat(datalist.getDeleted()).isFalse();

        DataListEntity person1 = getDataListEntityByName(datalist,"Text1");
        assertThat(person1).isNotNull();
        assertThat(person1.getDeleted()).isTrue();

        DataListEntity person2 = getDataListEntityByName(datalist,"Text4");
        assertThat(person2).isNotNull();
        assertThat(person2.getDeleted()).isFalse();
    }

    @Test
    public void testServiceUpdateDataListRemove() throws EntityNotFoundException {
        DataList datalist1 = getDataList();
        when(mockRepo.findOneByName(TEST_LIST)).thenReturn(datalist1);

        List<DataListEntityRecord> dataListEntitys = new ArrayList<>();
        DataList newDataList = new DataList(new DataListRecord(TEST_LIST, dataListEntitys));
        service.updateDataList(newDataList);

        verify(mockRepo).save(captor.capture());
        final DataList datalist = captor.getValue();

        verify(mockRepo, times(1)).save(any(DataList.class));
        assertThat(datalist).isNotNull();
        assertThat(datalist.getEntities()).hasSize(1);
        assertThat(datalist.getDeleted()).isTrue();

        DataListEntity person1 = getDataListEntityByName(datalist,"Text1");
        assertThat(person1).isNotNull();
        assertThat(person1.getDeleted()).isTrue();
    }

    @Test
    public void testServiceUpdateDataListBoth() throws EntityNotFoundException {
        DataListEntityRecord dataListEntity1 = new DataListEntityRecord("Text1", "Text1");
        DataListEntityRecord dataListEntity2 = new DataListEntityRecord("Text3", "Text3");
        DataListEntityRecord dataListEntity3 = new DataListEntityRecord("Text4", "Text4");

        List<DataListEntityRecord> datalistEntitys = new ArrayList<>();
        datalistEntitys.addAll(Arrays.asList(dataListEntity1, dataListEntity2, dataListEntity3));
        DataList newDataList1 = new DataList(new DataListRecord(TEST_LIST, datalistEntitys));
        service.updateDataList(newDataList1);

        verify(mockRepo, times(1)).save(newDataList1);
    }

    @Test
    public void testServiceUpdateDataListSame() throws EntityNotFoundException {
        DataListEntityRecord datalistEntity1 = new DataListEntityRecord("Text1","Text1");
        List<DataListEntityRecord> dataListEntitys = new ArrayList<>();
        dataListEntitys.addAll(Collections.singletonList(datalistEntity1));
        DataList newDataList1 = new DataList(new DataListRecord(TEST_LIST, dataListEntitys));
        service.updateDataList(newDataList1);

        verify(mockRepo, times(1)).save(newDataList1);
    }

    @Test
    public void testServiceUpdateDataListNothingNone() throws EntityNotFoundException {
        List<DataListEntityRecord> dataListEntitys = new ArrayList<>();
        DataList newDataList1 = new DataList(new DataListRecord(TEST_LIST, dataListEntitys));
        service.updateDataList(newDataList1);

        verify(mockRepo, times(1)).save(newDataList1);
    }

    private static Set<DataList> getDataLists() {
        List<DataListEntityRecord> dataListEntities1 = new ArrayList<>();
        DataListEntityRecord datalistEntity1 = new DataListEntityRecord("Text1", "Value1");

        dataListEntities1.add(datalistEntity1);
        DataList datalist1 = new DataList(new DataListRecord(TEST_LIST, dataListEntities1));

        List<DataListEntityRecord> dataListEntities2 = new ArrayList<>();
        DataListEntityRecord datalistEntity2 = new DataListEntityRecord("Text2", "Value2");
        DataListEntityRecord datalistEntity3 = new DataListEntityRecord("Text3", "Value3");

        dataListEntities2.add(datalistEntity2);
        dataListEntities2.add(datalistEntity3);
        DataList datalist2 = new DataList(new DataListRecord(TEST_LIST+"2", dataListEntities2));

        Set<DataList> dataLists = new HashSet<>();
        dataLists.addAll(Arrays.asList(datalist1, datalist2));
        return dataLists;
    }

    private static DataList getDataListChildDeleted(Boolean parent, Boolean child) {
        Set<DataListEntity> dataListEntities = new HashSet<>();
        DataListEntity dataListEntity = new DataListEntity(new DataListEntityRecord("Text1", "Text1"));
        dataListEntity.setDeleted(child);

        dataListEntities.add(dataListEntity);
        DataList datalist = new DataList(new DataListRecord(TEST_LIST, null));
        datalist.setEntities(dataListEntities);
        datalist.setDeleted(parent);

        return datalist;
    }

    private static DataList getDataList() {
        Set<DataListEntity> dataListEntities = new HashSet<>();
        DataListEntity dataListEntity = new DataListEntity(new DataListEntityRecord("Text1", "Text1"));

        dataListEntities.add(dataListEntity);
        DataList datalist = new DataList(new DataListRecord(TEST_LIST, null));
        datalist.setEntities(dataListEntities);
        return datalist;
    }

    private static DataListEntity getDataListEntityByName(DataList entities, String entityName)
    {
        return entities.getEntities().stream().filter(t -> t.getText().equals(entityName)).findFirst().orElse(null);
    }

}