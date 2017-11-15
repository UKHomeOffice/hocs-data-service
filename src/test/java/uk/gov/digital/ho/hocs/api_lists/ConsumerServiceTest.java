package uk.gov.digital.ho.hocs.api_lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.DataListRepository;
import uk.gov.digital.ho.hocs.model.DataList;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerServiceTest {

    private final int NUMBER_OF_LISTS = 6;

    private final static String LIST_LORDS = "lords_list";
    private final static String LIST_COMMONS = "commons_list";
    private final static String LIST_SCOTTISH_PARLIAMENT = "scottish_parliament_list";
    private final static String LIST_IRISH_PARLIAMENT = "irish_parliament_list";
    private final static String LIST_EUROPEAN_PARLIAMENT = "european_parliament_list";
    private final static String LIST_WELSH_ASSEMBLY = "welsh_assembly_list";

    @Mock
    private DataListRepository mockRepo;

    private ListConsumerService listConsumerService;

    private RestTemplate restTemplate = new RestTemplate();

    private ListConsumerConfiguration configuration = new ListConsumerConfiguration("",
            "",
            "",
            "");


    @Before
    public void setUp() {
        listConsumerService = new ListConsumerService(mockRepo, restTemplate, configuration);
    }

    @Test
    public void testRefreshListsFromApiDoesNotCallDeleteWhenNoListsFound() {

        when(mockRepo.findDataListByName(anyString())).thenReturn(null);

        listConsumerService.refreshListsFromAPI();

        verify(mockRepo, times(NUMBER_OF_LISTS)).findDataListByName(anyString());
        verify(mockRepo, times(0)).delete(any(DataList.class));
        verify(mockRepo, times(NUMBER_OF_LISTS - 1)).save(any(DataList.class));
    }

    @Test
    public void testRefreshListsFromApiCallsDeleteTheCorrectNumberOfTimesWhenListsExists() {

        when(mockRepo.findDataListByName(LIST_COMMONS)).thenReturn(new DataList());
        when(mockRepo.findDataListByName(LIST_LORDS)).thenReturn(new DataList());

        when(mockRepo.findDataListByName(LIST_SCOTTISH_PARLIAMENT)).thenReturn(null);
        when(mockRepo.findDataListByName(LIST_IRISH_PARLIAMENT)).thenReturn(null);
        when(mockRepo.findDataListByName(LIST_EUROPEAN_PARLIAMENT)).thenReturn(null);
        when(mockRepo.findDataListByName(LIST_WELSH_ASSEMBLY)).thenReturn(null);

        doNothing().when(mockRepo).delete(anyCollection());
        when(mockRepo.save(any(DataList.class))).thenReturn(new DataList());

        listConsumerService.refreshListsFromAPI();

        verify(mockRepo, times(NUMBER_OF_LISTS)).findDataListByName(anyString());
        verify(mockRepo, times(1)).delete(anyCollection());
        verify(mockRepo, times(NUMBER_OF_LISTS - 1)).save(any(DataList.class));
    }

    @Test
    public void testCommonsApiIngest() {}

    @Test
    public void testLordsApiIngest() {}

    @Test
    public void testScottishParliamentApiIngest() {}

    @Test
    public void testIrishParliamentApiIngest() {

        when(mockRepo.findDataListByName(LIST_COMMONS)).thenReturn(null);
        when(mockRepo.findDataListByName(LIST_LORDS)).thenReturn(null);
        when(mockRepo.findDataListByName(LIST_SCOTTISH_PARLIAMENT)).thenReturn(new DataList());
        when(mockRepo.findDataListByName(LIST_IRISH_PARLIAMENT)).thenReturn(null);
        when(mockRepo.findDataListByName(LIST_EUROPEAN_PARLIAMENT)).thenReturn(null);
        when(mockRepo.findDataListByName(LIST_WELSH_ASSEMBLY)).thenReturn(null);

        listConsumerService.refreshListsFromAPI();

    }

    @Test
    public void testEuropeanParliamentApiIngest() {}

}