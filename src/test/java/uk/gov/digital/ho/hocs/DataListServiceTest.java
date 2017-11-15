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
        when(mockRepo.findOneByName(TEST_LIST)).thenReturn(buildValidDataList());

        DataListRecord dataListRecord = service.getListByName(TEST_LIST);

        verify(mockRepo).findOneByName(TEST_LIST);

        assertThat(dataListRecord).isNotNull();
        assertThat(dataListRecord).isInstanceOf(DataListRecord.class);
        Assertions.assertThat(dataListRecord.getEntities()).size().isEqualTo(1);
        assertThat(dataListRecord.getName()).isEqualTo(TEST_LIST);
        assertThat(dataListRecord.getEntities().get(0).getText()).isEqualTo("Text");
        assertThat(dataListRecord.getEntities().get(0).getValue()).isEqualTo("VALUE");
    }

    @Test(expected = ListNotFoundException.class)
    public void testListNotFoundThrowsListNotFoundException() throws ListNotFoundException {

        DataListRecord dataListRecord = service.getListByName(UNAVAILABLE_RESOURCE);
        verify(mockRepo).findOneByName(UNAVAILABLE_RESOURCE);
        assertThat(dataListRecord).isNull();
    }

    @Test
    public void testCreateList() {
        service.createList(buildValidDataList());
        verify(mockRepo).save(buildValidDataList());
    }

    @Test(expected = EntityCreationException.class)
    public void testRepoDataIntegrityExceptionThrowsEntityCreationException() {

        DataList dataList = buildValidDataList();

        when(mockRepo.save(dataList)).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "list_name_idempotent")));
        service.createList(dataList);

        verify(mockRepo).save(dataList);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testRepoUnhandledExceptionThrowsDataIntegrityException() {

        DataList dataList = buildValidDataList();

        when(mockRepo.save(dataList)). thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "")));
        service.createList(dataList);

        verify(mockRepo).save(dataList);
    }

    private DataList buildValidDataList() {
        Set<DataListEntity> dataListEntities = new HashSet<>();
        DataListEntityProperty property = new DataListEntityProperty("caseType", "CaseValue");
        Set<DataListEntityProperty> properties = new HashSet<>();
        properties.add(property);
        DataListEntity dataListEntity = new DataListEntity("Text", "Value");
        dataListEntity.setProperties(properties);

        dataListEntities.add(dataListEntity);
        return new DataList(TEST_LIST, dataListEntities);
    }

}