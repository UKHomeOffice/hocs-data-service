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
import uk.gov.digital.ho.hocs.model.DataListEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DataListResourceTest {

    private static final String TEST_REFERENCE = "Reference";

    @Mock
    private DataListService dataListService;

    @Mock
    private MemberService memberService;

    private DataListResource dataListResource;

    @Before
    public void setUp() {
        dataListResource = new DataListResource(dataListService, memberService);
    }

    @Test
    public void shouldRetrieveAllEntities() throws IOException, JSONException, ListNotFoundException {

        DataListRecord dataList = new DataListRecord(TEST_REFERENCE, new ArrayList<>());

        when(dataListService.getListByName(TEST_REFERENCE)).thenReturn(dataList);
        ResponseEntity<DataListRecord> httpResponse = dataListResource.getListByName(TEST_REFERENCE);

        assertThat(httpResponse.getBody()).isEqualTo(dataList);
        assertThat(httpResponse.getBody().getName()).isEqualTo(TEST_REFERENCE);
        assertThat(httpResponse.getBody().getEntities()).isEmpty();
        verify(dataListService).getListByName(TEST_REFERENCE);
    }

    @Test
    public void shouldReturnNotFoundWhenUnableToFindEntity() throws ListNotFoundException {
        when(dataListService.getListByName(TEST_REFERENCE)).thenThrow(new ListNotFoundException());
        ResponseEntity<DataListRecord> httpResponse = dataListResource.getListByName(TEST_REFERENCE);

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(httpResponse.getBody()).isNull();
        verify(dataListService).getListByName(TEST_REFERENCE);
    }

    @Test
    public void shouldReturnBadRequestWhenUnableCreateDep() throws EntityCreationException {
        DataList emptyDataList = new DataList("", new HashSet<>());
        doThrow(new EntityCreationException("")).when(dataListService).createList(emptyDataList);
        ResponseEntity httpResponse = dataListResource.postList(emptyDataList);
        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(dataListService).createList(emptyDataList);
    }

    @Test
    public void shouldReturnOKtWhenAbleCreateDep() throws EntityCreationException {
        DataList emptyDataList = new DataList("", new HashSet<>());
        ResponseEntity httpResponse = dataListResource.postList(emptyDataList);
        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(dataListService).createList(emptyDataList);
    }

    @Test
    public void shouldReturnBadRequestWhenUnableCreate() throws EntityCreationException {
        Set<DataListEntity> emptyDataList = new HashSet<>();
        doThrow(new EntityCreationException("")).when(dataListService).createList(any());
        ResponseEntity httpResponse = dataListResource.postListByName("", emptyDataList);
        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(dataListService).createList(any());
    }

    @Test
    public void shouldReturnOKtWhenAbleCreate() throws EntityCreationException {
        Set<DataListEntity> emptyDataList = new HashSet<>();
        ResponseEntity httpResponse = dataListResource.postListByName("", emptyDataList);
        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(dataListService).createList(any());
    }
}
