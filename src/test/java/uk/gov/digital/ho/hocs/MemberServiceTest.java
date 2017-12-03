package uk.gov.digital.ho.hocs;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.exception.IngestException;
import uk.gov.digital.ho.hocs.ingest.members.*;
import uk.gov.digital.ho.hocs.model.DataList;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MemberServiceTest {

    @Mock
    private DataListRepository mockRepo;

    @Mock
    private ListConsumerService mockListConsumerService;

    private MemberService memberService;

    @Before
    public void setUp() {
        memberService = new MemberService(mockRepo, mockListConsumerService);
    }

    @Test
    public void testCommonsApiIngest() throws IngestException {
        List<Member> membersList = new ArrayList<>();
        Member firstMember = new Member("First Member", "Member, First", "commons" );
        membersList.add(firstMember);
        Members members = new Members(membersList);

        when(mockListConsumerService.createCommonsFromUKParliamentAPI()).thenReturn(members);

        memberService.createCommonsUKParliament();

        verify(mockListConsumerService, times(1)).createCommonsFromUKParliamentAPI();
        verify(mockRepo, times(1)).save(any(DataList.class));
    }

    @Test
    public void testLordsApiIngest() throws IngestException {
        List<Member> membersList = new ArrayList<>();
        Member firstMember = new Member("First Member", "Member, First", "lords" );
        membersList.add(firstMember);
        Members members = new Members(membersList);

        when(mockListConsumerService.createLordsFromUKParliamentAPI()).thenReturn(members);

        memberService.createLordsUKParliament();

        verify(mockListConsumerService, times(1)).createLordsFromUKParliamentAPI();
        verify(mockRepo, times(1)).save(any(DataList.class));
    }

    @Test
    public void testScottishParliamentApiIngest() throws IngestException {
        List<ScottishMember> membersList = new ArrayList<>();
        ScottishMember firstMember = new ScottishMember("Member, First");
        membersList.add(firstMember);
        ScottishMembers members = new ScottishMembers(membersList);

        when(mockListConsumerService.createFromScottishParliamentAPI()).thenReturn(members);

        memberService.createScottishParliament();

        verify(mockListConsumerService, times(1)).createFromScottishParliamentAPI();
        verify(mockRepo, times(1)).save(any(DataList.class));
    }

    @Test
    public void testIrishParliamentApiIngest() throws IngestException {
        List<IrishMember> membersList = new ArrayList<>();
        IrishMember firstMember = new IrishMember("Member, First");
        membersList.add(firstMember);
        IrishMembers members = new IrishMembers(membersList);

        when(mockListConsumerService.createFromIrishParliamentAPI()).thenReturn(members);

        memberService.createIrishParliament();

        verify(mockListConsumerService, times(1)).createFromIrishParliamentAPI();
        verify(mockRepo, times(1)).save(any(DataList.class));
    }

    @Test
    public void testEuropeanParliamentApiIngest() throws IngestException {
        List<EuropeMember> membersList = new ArrayList<>();
        EuropeMember firstMember = new EuropeMember("First MEMBER", "United Kingdom");
        membersList.add(firstMember);
        EuropeMembers members = new EuropeMembers(membersList);

        when(mockListConsumerService.createFromEuropeanParliamentAPI()).thenReturn(members);

        memberService.createEuropeanParliament();

        verify(mockListConsumerService, times(1)).createFromEuropeanParliamentAPI();
        verify(mockRepo, times(1)).save(any(DataList.class));
    }

}