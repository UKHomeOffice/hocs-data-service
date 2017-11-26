package uk.gov.digital.ho.hocs.ingest.members;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.DataListRepository;
import uk.gov.digital.ho.hocs.model.DataList;
import uk.gov.digital.ho.hocs.model.DataListEntity;
import uk.gov.digital.ho.hocs.model.DataListEntityProperty;

import java.util.ArrayList;
import java.util.List;

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

    private final String UK_PARLIAMENT_ENDPOINT = "http://test.url/uk/%s";
    private final String SCOTTISH_PARLIAMENT_ENDPOINT = "http://test.url/scot/";
    private final String NORTHERN_IRISH_ASSEMBLY_ENDPOINT = "http://test.url/nia/";
    private final String EU_PARLIAMENT_ENDPOINT = "http://test.url/uk/eu/";

    @Mock
    private DataListRepository mockRepo;

    private ListConsumerService listConsumerService;

    @Mock
    private RestTemplate restTemplate;

    private ListConsumerConfigurator configuration = new ListConsumerConfigurator(
            UK_PARLIAMENT_ENDPOINT,
            SCOTTISH_PARLIAMENT_ENDPOINT,
            NORTHERN_IRISH_ASSEMBLY_ENDPOINT,
            EU_PARLIAMENT_ENDPOINT
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
        verify(mockRepo, times(0)).save(any(DataList.class));
    }

    @Test
    public void testRefreshListsFromApiCallsDeleteWhenListsExists() {

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
        verify(mockRepo, times(0)).save(any(DataList.class));
    }

    @Test
    public void testCommonsApiIngest() {
        List<Member> membersList = new ArrayList<>();
        Member firstMember = new Member("First Member", "Member, First", "commons" );

        membersList.add(firstMember);

        Members members = new Members(membersList);

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
            Assert.assertEquals("commons_list", dataList.getName());

            List<DataListEntity> dataListEntities = new ArrayList<>(dataList.getEntities());
            DataListEntity member = dataListEntities.get(0);

            Assert.assertEquals("First Member", member.getText());
            Assert.assertEquals("MEMBER_FIRST", member.getValue());

            List<DataListEntityProperty> dataListEntityProperties = new ArrayList<>(member.getProperties());
            DataListEntityProperty house = dataListEntityProperties.get(0);

            Assert.assertEquals("house", house.getKey());
            Assert.assertEquals("commons", house.getValue());

            return null;

        });

        listConsumerService.createFromUKParliamentAPI("commons");

        verify(mockRepo, times(1)).save(any(DataList.class));
    }

    @Test
    public void testLordsApiIngest() {
        List<Member> membersList = new ArrayList<>();
        Member firstMember = new Member("First Member", "Member, First", "lords" );

        membersList.add(firstMember);

        Members members = new Members(membersList);

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
            Assert.assertEquals("lords_list", dataList.getName());

            List<DataListEntity> dataListEntities = new ArrayList<>(dataList.getEntities());
            DataListEntity member = dataListEntities.get(0);

            Assert.assertEquals("First Member", member.getText());
            Assert.assertEquals("MEMBER_FIRST", member.getValue());

            List<DataListEntityProperty> dataListEntityProperties = new ArrayList<>(member.getProperties());
            DataListEntityProperty house = dataListEntityProperties.get(0);

            Assert.assertEquals("house", house.getKey());
            Assert.assertEquals("lords", house.getValue());

            return null;

        });

        listConsumerService.createFromUKParliamentAPI("lords");

        verify(mockRepo, times(1)).save(any(DataList.class));
    }

    @Test
    public void testScottishParliamentApiIngest() {
        List<ScottishMember> membersList = new ArrayList<>();
        ScottishMember firstMember = new ScottishMember("Member, First");

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
        IrishMember firstMember = new IrishMember("Member, First");
        membersList.add(firstMember);

        IrishMembers members = new IrishMembers(membersList);

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
    public void testEuropeanParliamentApiIngest() {
        List<EuropeMember> membersList = new ArrayList<>();
        EuropeMember firstMember = new EuropeMember("First MEMBER", "United Kingdom");

        membersList.add(firstMember);

        EuropeMembers members = new EuropeMembers(membersList);

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
            Assert.assertEquals("european_parliament_list", dataList.getName());

            List<DataListEntity> dataListEntities = new ArrayList<>(dataList.getEntities());
            DataListEntity member = dataListEntities.get(0);

            Assert.assertEquals("First Member", member.getText());
            Assert.assertEquals("MEMBER_FIRST", member.getValue());

            List<DataListEntityProperty> dataListEntityProperties = new ArrayList<>(member.getProperties());
            DataListEntityProperty house = dataListEntityProperties.get(0);

            Assert.assertEquals("house", house.getKey());
            Assert.assertEquals("european_parliament", house.getValue());

            return null;

        });

        listConsumerService.createFromEuropeanParliamentAPI();

        verify(mockRepo, times(1)).save(any(DataList.class));
    }

}