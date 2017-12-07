package uk.gov.digital.ho.hocs;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.dto.legacy.users.UserCreateRecord;
import uk.gov.digital.ho.hocs.exception.AlfrescoPostException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AlfrescoServiceTest {

    private final String API_USERNAME = "admin";
    private final String API_PASSWORD = "admin";
    private final String API_HOST = "http://localhost";

    @Mock
    private RestTemplate restTemplate;

    private AlfrescoService service;

    private AlfrescoConfiguration configuration = new AlfrescoConfiguration(API_USERNAME, API_PASSWORD, API_HOST);


    @Before
    public void setUp() {
        service = new AlfrescoService(restTemplate, configuration);
    }

    @Test
    public void testPostBatchedRecords() throws AlfrescoPostException{

        List<UserCreateRecord> recordsList = getRecordSet();
        Integer numberOfRecords = recordsList.size();

        doReturn(ResponseEntity.ok(recordsList)).when(restTemplate).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        );

        service.postBatchedRecords(recordsList);

        verify(restTemplate, times(numberOfRecords)).exchange(
                eq(API_HOST + AlfrescoConfiguration.API_ENDPOINT_USERS),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        );

    }

    @Test(expected = AlfrescoPostException.class)
    public void testGroupListNotFoundThrowsListNotFoundException() throws AlfrescoPostException {

        List<UserCreateRecord> recordsList = getRecordSet();

        doReturn(ResponseEntity.badRequest().build()).when(restTemplate).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        );

        service.postBatchedRecords(recordsList);

    }

    private List<UserCreateRecord> getRecordSet() {

        List<UserCreateRecord> recordsList = new ArrayList<>();
        UserCreateRecord record = new UserCreateRecord(
                new HashSet<>(), new HashSet<>(), new ArrayList<>()
        );

        Integer numberOfRecords = (int) (Math.random() * 10 + 1);

        for (int i = 0; i < numberOfRecords; i++) {
            recordsList.add(record);
        }

        return recordsList;
    }


}