package uk.gov.digital.ho.hocs;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.dto.DataListRecord;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.model.DataList;

import java.io.IOException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DataListResourceTest {

    private static final String TEST_REFERENCE = "Reference";

    @Mock
    private DataListService dataListService;

    @Mock
    private HouseService houseService;

    private DataListResource dataListResource;

    @Before
    public void setUp() {
        dataListResource = new DataListResource(dataListService, houseService);
    }

    @Test
    public void shouldRetrieveAllEntities() throws IOException, JSONException, ListNotFoundException {

        DataList dataList = new DataList(new DataListRecord(TEST_REFERENCE, new ArrayList<>()));

        when(dataListService.getDataListByName(TEST_REFERENCE)).thenReturn(dataList);
        ResponseEntity<DataListRecord> httpResponse = dataListResource.getListByName(TEST_REFERENCE);

        assertThat(httpResponse.getBody().getName()).isEqualTo(TEST_REFERENCE);
        assertThat(httpResponse.getBody().getEntities()).isEmpty();
        verify(dataListService).getDataListByName(TEST_REFERENCE);
    }

    @Test
    public void shouldReturnNotFoundWhenUnableToFindEntity() throws ListNotFoundException {
        when(dataListService.getDataListByName(TEST_REFERENCE)).thenThrow(new ListNotFoundException());
        ResponseEntity<DataListRecord> httpResponse = dataListResource.getListByName(TEST_REFERENCE);

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(httpResponse.getBody()).isNull();
        verify(dataListService).getDataListByName(TEST_REFERENCE);
    }

    @Test
    public void shouldReturnBadRequestWhenUnableCreateDep() throws EntityCreationException {
        DataList emptyDataList = new DataList();
        doThrow(new EntityCreationException("")).when(dataListService).updateDataList(emptyDataList);

        ResponseEntity httpResponse = dataListResource.postList(DataListRecord.create(emptyDataList));
        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(dataListService).updateDataList(emptyDataList);
    }

    @Test
    public void shouldReturnOKtWhenAbleCreateDep() throws EntityCreationException {
        DataList emptyDataList = new DataList();
        ResponseEntity httpResponse = dataListResource.postList(DataListRecord.create(emptyDataList));
        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(dataListService).updateDataList(emptyDataList);
    }
}
