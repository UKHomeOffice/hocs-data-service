package uk.gov.digital.ho.hocs;

import org.assertj.core.api.Assertions;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;
import uk.gov.digital.ho.hocs.dto.DataListRecord;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.model.DataList;
import uk.gov.digital.ho.hocs.model.DataListEntity;
import uk.gov.digital.ho.hocs.model.DataListEntityProperty;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataListServiceTest {

    private final static String TEST_LIST = "Test List One";
    private final static String LIST_A = "LIST_A";
    private final static String LIST_B = "LIST_B";
    private final static String LIST_C = "LIST_C";
    private final static String UNAVAILABLE_RESOURCE = "Unavailable Resource";

    @Mock
    private DataListRepository mockRepo;

    private DataListService service;


    @Before
    public void setUp() {
        service = new DataListService(mockRepo);
    }

    @Test
    public void testCollaboratorsGettingList() throws ListNotFoundException {
        when(mockRepo.findOneByName(TEST_LIST)).thenReturn(buildValidDataList(TEST_LIST));

        DataListRecord dataListRecord = service.getListByName(TEST_LIST);

        verify(mockRepo).findOneByName(TEST_LIST);

        assertThat(dataListRecord).isNotNull();
        assertThat(dataListRecord).isInstanceOf(DataListRecord.class);
        assertThat(dataListRecord.getEntities()).size().isEqualTo(1);
        assertThat(dataListRecord.getName()).isEqualTo(TEST_LIST);
        assertThat(dataListRecord.getEntities().get(0).getText()).isEqualTo("Text");
        assertThat(dataListRecord.getEntities().get(0).getValue()).isEqualTo("VALUE");
    }

    @Test
    public void testGetCombinedList() throws ListNotFoundException {

        when(mockRepo.findOneByName(LIST_A)).thenReturn(buildValidDataList(LIST_A));
        when(mockRepo.findOneByName(LIST_B)).thenReturn(buildValidDataList(LIST_B));

        DataListRecord dataListRecord = service.getCombinedList(LIST_C, LIST_A, LIST_B);

        verify(mockRepo).findOneByName(LIST_A);
        verify(mockRepo).findOneByName(LIST_B);

        assertThat(dataListRecord).isNotNull();
        assertThat(dataListRecord).isInstanceOf(DataListRecord.class);
        assertThat(dataListRecord.getName()).isEqualTo(LIST_C);
        assertThat(dataListRecord.getEntities().size()).isEqualTo(2);

    }

    @Test(expected = ListNotFoundException.class)
    public void testListNotFoundThrowsListNotFoundException() throws ListNotFoundException {

        DataListRecord dataListRecord = service.getListByName(UNAVAILABLE_RESOURCE);
        verify(mockRepo).findOneByName(UNAVAILABLE_RESOURCE);
        assertThat(dataListRecord).isNull();
    }

    @Test
    public void testCreateList() {
        service.createList(buildValidDataList(TEST_LIST));
        verify(mockRepo).save(buildValidDataList(TEST_LIST));
    }

    @Test(expected = EntityCreationException.class)
    public void testRepoDataIntegrityExceptionThrowsEntityCreationException() {

        DataList dataList = buildValidDataList(TEST_LIST);

        when(mockRepo.save(dataList)).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "list_name_idempotent")));
        service.createList(dataList);

        verify(mockRepo).save(dataList);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testRepoUnhandledExceptionThrowsDataIntegrityException() {

        DataList dataList = buildValidDataList(TEST_LIST);

        when(mockRepo.save(dataList)). thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "")));
        service.createList(dataList);

        verify(mockRepo).save(dataList);
    }

    private DataList buildValidDataList(String name) {
        Set<DataListEntity> dataListEntities = new HashSet<>();
        DataListEntityProperty property = new DataListEntityProperty("caseType", "CaseValue");
        Set<DataListEntityProperty> properties = new HashSet<>();
        properties.add(property);
        DataListEntity dataListEntity = new DataListEntity("Text", "Value");
        dataListEntity.setProperties(properties);

        dataListEntities.add(dataListEntity);
        return new DataList(name, dataListEntities);
    }

}