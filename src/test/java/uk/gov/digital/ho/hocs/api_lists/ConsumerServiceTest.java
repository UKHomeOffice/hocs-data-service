package uk.gov.digital.ho.hocs.api_lists;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.DataListRepository;
import uk.gov.digital.ho.hocs.model.DataList;
import uk.gov.digital.ho.hocs.model.DataListEntity;
import uk.gov.digital.ho.hocs.model.DataListEntityProperty;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Mock
    private RestTemplate restTemplate;

    private ListConsumerConfigurator configuration = new ListConsumerConfigurator(
            "http://test.url",
            "http://test.url",
            "http://test.url",
            "http://test.url"
    );


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
        when(mockRepo.save(any(DataList.class))).thenReturn(null);

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
    public void testScottishParliamentApiIngest() {
        List<ScottishMember> membersList = new ArrayList<>();
        ScottishMember firstMember = new ScottishMember();
        firstMember.setName("Member, First");

        membersList.add(firstMember);

        ResponseEntity response = ResponseEntity.ok(membersList.toArray());

        doReturn(response).when(restTemplate).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(Class.class)
        );

        when(mockRepo.save(any(DataList.class))).thenAnswer(i -> {

            DataList dataList = i.getArgumentAt(0, DataList.class);

            Assert.assertNotNull(dataList);
            Assert.assertEquals(1, dataList.getEntities().size());
            Assert.assertEquals("scottish_parliament_list", dataList.getName());

            List<DataListEntity> dataListEntities = new ArrayList<>(dataList.getEntities());
            DataListEntity member = dataListEntities.get(0);

            Assert.assertEquals("First Member", member.getText());
            Assert.assertEquals("MEMBER_FIRST", member.getValue());

            List<DataListEntityProperty> dataListEntityProperties = new ArrayList<>(member.getProperties());
            DataListEntityProperty house = dataListEntityProperties.get(0);

            Assert.assertEquals("house", house.getKey());
            Assert.assertEquals("scottish_parliament", house.getValue());

            return null;

        });

        listConsumerService.createFromScottishParliamentAPI();

        verify(mockRepo, times(1)).save(any(DataList.class));
    }

    @Test
    public void testIrishParliamentApiIngest() {

        List<IrishMember> membersList = new ArrayList<>();
        IrishMember firstMember = new IrishMember();
        firstMember.setName("Member, First");

        membersList.add(firstMember);

        IrishMembers members = new IrishMembers();
        members.setMembers(membersList);

        ResponseEntity response = ResponseEntity.ok(members);

        doReturn(response).when(restTemplate).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(Class.class)
        );

        when(mockRepo.save(any(DataList.class))).thenAnswer(i -> {

            DataList dataList = i.getArgumentAt(0, DataList.class);

            Assert.assertNotNull(dataList);
            Assert.assertEquals(1, dataList.getEntities().size());
            Assert.assertEquals("northern_irish_assembly_list", dataList.getName());

            List<DataListEntity> dataListEntities = new ArrayList<>(dataList.getEntities());
            DataListEntity member = dataListEntities.get(0);

            Assert.assertEquals("First Member", member.getText());
            Assert.assertEquals("MEMBER_FIRST", member.getValue());

            List<DataListEntityProperty> dataListEntityProperties = new ArrayList<>(member.getProperties());
            DataListEntityProperty house = dataListEntityProperties.get(0);

            Assert.assertEquals("house", house.getKey());
            Assert.assertEquals("northern_irish_assembly", house.getValue());

            return null;

        });

        listConsumerService.createFromIrishParliamentAPI();

        verify(mockRepo, times(1)).save(any(DataList.class));

    }

    @Test
    public void testEuropeanParliamentApiIngest() {}

}