package uk.gov.digital.ho.hocs;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.ingest.members.ListConsumerService;
import uk.gov.digital.ho.hocs.model.House;
import uk.gov.digital.ho.hocs.model.Member;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MemberServiceTest {

    private final static String HOUSENAME = "Test";
    private final static String UNAVAILABLE_RESOURCE = "Unavailable Resource";

    @Mock
    private MemberRepository mockRepo;

    @Mock
    private ListConsumerService listConsumerService;

    @Captor
    private ArgumentCaptor<House> captor;

    private MemberService memberService;


    @Before
    public void setUp() {
        memberService = new MemberService(mockRepo, listConsumerService);
    }

    @Test
    public void testCollaboratorsGettingHouses() throws ListNotFoundException {
        when(mockRepo.findAllByDeletedIsFalse()).thenReturn(getHouses());

        List<House> records = memberService.getAllHouses().stream().collect(Collectors.toList());

        verify(mockRepo).findAllByDeletedIsFalse();

        assertThat(records).isNotNull();
        assertThat(records).hasOnlyElementsOfType(House.class);
        assertThat(records).hasSize(2);

        assertThat(records.get(1).getName()).isEqualTo(HOUSENAME);
        assertThat(records.get(1).getMembers()).hasSize(1);

        assertThat(records.get(0).getName()).isEqualTo(HOUSENAME+"2");
        assertThat(records.get(0).getMembers()).hasSize(2);
    }

    @Test(expected = ListNotFoundException.class)
    public void testLegacyListNotFoundThrowsListNotFoundException() throws ListNotFoundException {

        List<House> records = memberService.getAllHouses().stream().collect(Collectors.toList());
        verify(mockRepo).findAllByDeletedIsFalse();
        assertThat(records).isEmpty();
    }

    @Test
    public void testCollaboratorsGettingHouse() throws ListNotFoundException {
        when(mockRepo.findOneByNameAndDeletedIsFalse(HOUSENAME)).thenReturn(getHouse());

        House record = memberService.getHouseByName(HOUSENAME);

        verify(mockRepo).findOneByNameAndDeletedIsFalse(HOUSENAME);

        assertThat(record).isNotNull();
        assertThat(record.getName()).isEqualTo(HOUSENAME);
        assertThat(record.getMembers()).hasSize(1);
    }

    @Test(expected = ListNotFoundException.class)
    public void testAllListNotFoundThrowsListNotFoundException() throws ListNotFoundException {

        House record = memberService.getHouseByName(UNAVAILABLE_RESOURCE);
        verify(mockRepo).findOneByNameAndDeletedIsFalse(UNAVAILABLE_RESOURCE);
        assertThat(record).isNull();
    }

    @Test
    public void testCreateList() {
        memberService.updateHouse(getHouse());
        verify(mockRepo).save(any(House.class));
    }


    @Test(expected = EntityCreationException.class)
    public void testCreateListNull() {
        memberService.updateHouse(null);
        verify(mockRepo, times(0)).save(anyList());
    }

    @Test
    public void testCreateListNoEntities() {
        memberService.updateHouse(new House(HOUSENAME, new HashSet<>()));
        verify(mockRepo, times(0)).save(anyList());
    }
    
    @Test(expected = EntityCreationException.class)
    public void testRepoDataIntegrityExceptionThrowsEntityCreationException() {

        House house1 = getHouse();

        when(mockRepo.save(house1)).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "house_name_idempotent")));
        memberService.updateHouse(house1);

        verify(mockRepo).save(house1);
    }

    @Test(expected = EntityCreationException.class)
    public void testRepoDataIntegrityExceptionThrowsEntityCreationExceptionTwo() {

        House house1 = getHouse();

        when(mockRepo.save(house1)).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "member_name_ref_idempotent")));
        memberService.updateHouse(house1);

        verify(mockRepo).save(house1);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testRepoDataIntegrityExceptionThrowsDataIntegrityViolationException() {

        House house1 = getHouse();

        when(mockRepo.save(house1)).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "")));
        memberService.updateHouse(house1);

        verify(mockRepo).save(house1);
    }

    @Test
    public void testServiceUpdateHouseAdd() throws ListNotFoundException {
        House house1 = getHouse();
        
        when(mockRepo.findOneByName(HOUSENAME)).thenReturn(house1);

        Member member1 = new Member("Person1");
        Member member2 = new Member("Person2");
        Member member3 = new Member("Person3");
        Set<Member> members = new HashSet<>();
        members.addAll(Arrays.asList(member1, member2, member3));
        House house = new House(HOUSENAME, members);
        memberService.updateHouse(house);

        verify(mockRepo, times(1)).save(house);
    }

    @Test
    public void testServiceUpdateHouseAddWhenAlreadyDeleted() {
        House house1 = getHouseChildDeleted(true, true);
        when(mockRepo.findOneByName(HOUSENAME)).thenReturn(house1);

        Member member1 = new Member("Person1");
        Member member2 = new Member("Person2");
        Set<Member> members = new HashSet<>();
        members.add(member1);
        members.add(member2);
        House newHouse = new House(HOUSENAME, members);
        memberService.updateHouse(newHouse);

        verify(mockRepo).save(captor.capture());
        final House house = captor.getValue();

        verify(mockRepo, times(1)).save(newHouse);
        assertThat(house).isNotNull();
        assertThat(house.getMembers()).hasSize(2);
        assertThat(house.getDeleted()).isFalse();

        Member person1 = getMemberByName(house,"Person1");
        assertThat(person1).isNotNull();
        assertThat(person1.getDeleted()).isFalse();
        

        Member person2 = getMemberByName(house,"Person2");
        assertThat(person2).isNotNull();
        assertThat(person2.getDeleted()).isFalse();
        
    }

    @Test
    public void testServiceUpdateHouseEmptyGroupIsDeleted() {
        House house1 = getHouseChildDeleted(true, true);
        when(mockRepo.findOneByName(HOUSENAME)).thenReturn(house1);

        Set<Member> members = new HashSet<>();
        members.add(new Member("Person4"));
        House newHouse = new House(HOUSENAME, members);
        memberService.updateHouse(newHouse);

        verify(mockRepo).save(captor.capture());
        final House house = captor.getValue();

        verify(mockRepo, times(1)).save(newHouse);
        assertThat(house).isNotNull();
        assertThat(house.getMembers()).hasSize(2);
        assertThat(house.getDeleted()).isFalse();

        Member person1 = getMemberByName(house,"Person1");
        assertThat(person1).isNotNull();
        assertThat(person1.getDeleted()).isTrue();

        Member person2 = getMemberByName(house,"Person4");
        assertThat(person2).isNotNull();
        assertThat(person2.getDeleted()).isFalse();
    }

    @Test
    public void testServiceUpdateHouseRemove() throws ListNotFoundException {
        House house1 = getHouse();
        when(mockRepo.findOneByName(HOUSENAME)).thenReturn(house1);

        Set<Member> members = new HashSet<>();
        House newHouse = new House(HOUSENAME, members);
        memberService.updateHouse(newHouse);

        verify(mockRepo).save(captor.capture());
        final House house = captor.getValue();

        verify(mockRepo, times(1)).save(newHouse);
        assertThat(house).isNotNull();
        assertThat(house.getMembers()).hasSize(1);
        assertThat(house.getDeleted()).isTrue();

        Member person1 = getMemberByName(house,"Person1");
        assertThat(person1).isNotNull();
        assertThat(person1.getDeleted()).isTrue();

    }

    @Test
    public void testServiceUpdateHouseBoth() throws ListNotFoundException {
        House house1 = getHouse();
        when(mockRepo.findOneByName("Dept")).thenReturn(house1);

        Member member1 = new Member("Person1");
        Member member2 = new Member("Person3");
        Member member3 = new Member("Person4");

        Set<Member> members = new HashSet<>();
        members.addAll(Arrays.asList(member1, member2, member3));
        House newHouse1 = new House(HOUSENAME, members);
        memberService.updateHouse(newHouse1);

        verify(mockRepo, times(1)).save(newHouse1);
    }

    @Test
    public void testServiceUpdateHouseSame() throws ListNotFoundException {
        House house1 = getHouse();
        when(mockRepo.findOneByName("Dept")).thenReturn(house1);

        Member member1 = new Member("Person1");
        Set<Member> members = new HashSet<>();
        members.addAll(Arrays.asList(member1));
        House newHouse1 = new House(HOUSENAME, members);
        memberService.updateHouse(newHouse1);

        verify(mockRepo, times(1)).save(newHouse1);
    }

    @Test
    public void testServiceUpdateHouseNothingNone() throws ListNotFoundException {
        House house1 = getHouse();
        when(mockRepo.findOneByName("Dept")).thenReturn(house1);

        Set<Member> members = new HashSet<>();
        House newHouse1 = new House(HOUSENAME, members);
        memberService.updateHouse(newHouse1);

        verify(mockRepo, times(1)).save(newHouse1);
    }

    private static Set<House> getHouses() {
        Member member1 = new Member("Person1");

        Set<Member> members1 = new HashSet<>();
        members1.add(member1);

        House house1 = new House(HOUSENAME, members1);

        Member member2 = new Member("Person2");
        Member member3 = new Member("Person3");

        Set<Member> members2 = new HashSet<>();
        members2.addAll(Arrays.asList(member2, member3));

        House house2 = new House(HOUSENAME+"2", members2);

        Set<House> houses = new HashSet<>();
        houses.addAll(Arrays.asList(house1, house2));
        return houses;
    }

    private static House getHouseChildDeleted(Boolean parent, Boolean child) {
        Member member1 = new Member("Person1");
        member1.setDeleted(child);

        Set<Member> members = new HashSet<>();
        members.addAll(Arrays.asList(member1));

        House house = new House(HOUSENAME, members);
        house.setDeleted(parent);

        return house;
    }

    
    private static House getHouse() {
        Member member1 = new Member("Person1");

        Set<Member> members = new HashSet<>();
        members.add(member1);

        House house = new House(HOUSENAME, members);

        return house;
    }

    private static Member getMemberByName(House persons, String personName)
    {
        return persons.getMembers().stream().filter(t -> t.getDisplayName().equals(personName)).findFirst().orElse(null);
    }

}