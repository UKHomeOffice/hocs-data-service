package uk.gov.digital.ho.hocs.ingest.members;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.exception.IngestException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerServiceTest {

    private final String UK_PARLIAMENT_ENDPOINT = "http://test.url/uk/%s";
    private final String SCOTTISH_PARLIAMENT_ENDPOINT = "http://test.url/scot/";
    private final String NORTHERN_IRISH_ASSEMBLY_ENDPOINT = "http://test.url/nia/";
    private final String EU_PARLIAMENT_ENDPOINT = "http://test.url/uk/eu/";


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
        listConsumerService = new ListConsumerService(restTemplate, configuration);
    }

    @Test
    public void testCommonsApiIngest() throws IngestException {
        List<Member> membersList = new ArrayList<>();
        Member firstMember = new Member("First Member", "Member, First", "commons" );
        membersList.add(firstMember);
        Members members = new Members(membersList);

        doReturn(ResponseEntity.ok(members)).when(restTemplate).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Members.class)
        );

        Members toTest =  listConsumerService.createCommonsFromUKParliamentAPI();

        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(Members.class));

        assertThat(toTest.getMembers()).hasSize(1);
        Member memberToTest = toTest.getMembers().get(0);

        assertThat(memberToTest.getDisplayName()).isEqualTo("First Member");
        assertThat(memberToTest.getListName()).isEqualTo("Member, First");
        assertThat(memberToTest.getHouse()).isEqualTo("commons");
    }

    @Test
    public void testLordsApiIngest() throws IngestException {
        List<Member> membersList = new ArrayList<>();
        Member firstMember = new Member("First Member", "Member, First", "lords" );
        membersList.add(firstMember);
        Members members = new Members(membersList);

        doReturn(ResponseEntity.ok(members)).when(restTemplate).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Members.class)
        );

        Members toTest = listConsumerService.createLordsFromUKParliamentAPI();

        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(Members.class));

        assertThat(toTest.getMembers()).hasSize(1);
        Member memberToTest = toTest.getMembers().get(0);

        assertThat(memberToTest.getDisplayName()).isEqualTo("First Member");
        assertThat(memberToTest.getListName()).isEqualTo("Member, First");
        assertThat(memberToTest.getHouse()).isEqualTo("lords");

   }

    @Test
    public void testScottishParliamentApiIngest() throws IngestException {
        List<ScottishMember> membersList = new ArrayList<>();
        ScottishMember firstMember = new ScottishMember("Member, First");
        membersList.add(firstMember);

        doReturn(ResponseEntity.ok(membersList.toArray())).when(restTemplate).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(ScottishMember[].class)
        );

        ScottishMembers toTest = listConsumerService.createFromScottishParliamentAPI();

        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(ScottishMember[].class));

        assertThat(toTest.getMembers()).hasSize(1);
        ScottishMember memberToTest = toTest.getMembers().get(0);

        assertThat(memberToTest.getName()).isEqualTo("Member, First");
    }

    @Test
    public void testIrishParliamentApiIngest() throws IngestException {
        List<IrishMember> membersList = new ArrayList<>();
        IrishMember firstMember = new IrishMember("Member, First");
        membersList.add(firstMember);
        IrishMembers members = new IrishMembers(membersList);

        doReturn(ResponseEntity.ok(members)).when(restTemplate).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(IrishMembers.class)
        );

        IrishMembers toTest = listConsumerService.createFromIrishParliamentAPI();

        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(IrishMembers.class));

        assertThat(toTest.getMembers()).hasSize(1);
        IrishMember memberToTest = toTest.getMembers().get(0);

        assertThat(memberToTest.getName()).isEqualTo("Member, First");
    }

    @Test
    public void testEuropeanParliamentApiIngest() throws IngestException {
        List<EuropeMember> membersList = new ArrayList<>();
        EuropeMember firstMember = new EuropeMember("First MEMBER", "United Kingdom");
        membersList.add(firstMember);
        EuropeMembers members = new EuropeMembers(membersList);

        doReturn(ResponseEntity.ok(members)).when(restTemplate).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(EuropeMembers.class)
        );

        EuropeMembers toTest = listConsumerService.createFromEuropeanParliamentAPI();

        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(EuropeMembers.class));

        assertThat(toTest.getMembers()).hasSize(1);
        EuropeMember memberToTest = toTest.getMembers().get(0);

        assertThat(memberToTest.getName()).isEqualTo("First MEMBER");
        assertThat(memberToTest.getCountry()).isEqualTo("United Kingdom");
    }

}