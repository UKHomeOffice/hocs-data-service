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
import uk.gov.digital.ho.hocs.ingest.topics.CSVTopicLine;
import uk.gov.digital.ho.hocs.model.Topic;
import uk.gov.digital.ho.hocs.model.TopicGroup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TopicsServiceTest {

    private final static String CASETYPE = "Test";
    private final static String UNAVAILABLE_RESOURCE = "Unavailable Resource";

    @Mock
    private TopicsRepository mockRepo;

    @Captor
    private ArgumentCaptor<HashSet<TopicGroup>> captor;

    private TopicsService topicsService;


    @Before
    public void setUp() {
        topicsService = new TopicsService(mockRepo);
    }

    @Test
    public void testCollaboratorsGettingTopics() throws ListNotFoundException {
        when(mockRepo.findAllByDeletedIsFalse()).thenReturn(buildTopicList());

        List<TopicGroup> records = topicsService.getAllTopics().stream().collect(Collectors.toList());

        verify(mockRepo).findAllByDeletedIsFalse();

        assertThat(records).isNotNull();
        assertThat(records).hasOnlyElementsOfType(TopicGroup.class);
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getName()).isEqualTo("TopicName");
        assertThat(records.get(0).getCaseType()).isEqualTo("CaseType");
    }

    @Test
    public void testCollaboratorsGettingAllTopics() throws ListNotFoundException {
        when(mockRepo.findAllByCaseTypeAndDeletedIsFalse(CASETYPE)).thenReturn(buildTopicList());

        List<TopicGroup> records = topicsService.getTopicByCaseType(CASETYPE).stream().collect(Collectors.toList());

        verify(mockRepo).findAllByCaseTypeAndDeletedIsFalse(CASETYPE);

        assertThat(records).isNotNull();
        assertThat(records).hasOnlyElementsOfType(TopicGroup.class);
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getName()).isEqualTo("TopicName");
        assertThat(records.get(0).getCaseType()).isEqualTo("CaseType");
    }

    @Test(expected = ListNotFoundException.class)
    public void testAllListNotFoundThrowsListNotFoundException() throws ListNotFoundException {

        List<TopicGroup> records = topicsService.getTopicByCaseType(UNAVAILABLE_RESOURCE).stream().collect(Collectors.toList());
        verify(mockRepo).findAllByCaseType(UNAVAILABLE_RESOURCE);
        assertThat(records).isEmpty();
    }

    @Test
    public void testCreateList() {
        topicsService.updateTopics(buildValidCSVTopicLines(), CASETYPE);
        verify(mockRepo).save(anyList());
    }

    @Test(expected = EntityCreationException.class)
    public void testCreateListNull() {
        topicsService.updateTopics(null, CASETYPE);
        verify(mockRepo, times(0)).save(anyList());
    }

    @Test
    public void testCreateListNoEntities() {
        topicsService.updateTopics(new HashSet<>(), CASETYPE);
        verify(mockRepo, times(0)).save(anyList());
    }

    @Test(expected = EntityCreationException.class)
    public void testRepoDataIntegrityExceptionThrowsEntityCreationException() {

        Set<CSVTopicLine> topics = buildValidCSVTopicLines();

        when(mockRepo.save(anyList())).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "topic_group_name_idempotent")));
        topicsService.updateTopics(topics, CASETYPE);

        verify(mockRepo).save(anyList());
    }

    @Test(expected = EntityCreationException.class)
    public void testRepoDataIntegrityExceptionThrowsEntityCreationExceptionTwo() {

        Set<CSVTopicLine> topics = buildValidCSVTopicLines();

        when(mockRepo.save(anyList())).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "topic_name_ref_idempotent")));
        topicsService.updateTopics(topics, CASETYPE);

        verify(mockRepo).save(anyList());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testRepoDataIntegrityExceptionThrowsDataIntegrityViolationException() {

        Set<CSVTopicLine> topics = buildValidCSVTopicLines();

        when(mockRepo.save(anyList())).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "")));
        topicsService.updateTopics(topics, CASETYPE);

        verify(mockRepo).save(anyList());
    }

    @Test
    public void testServiceUpdateTopicsFromCSVAdd() throws ListNotFoundException {
        Set<TopicGroup> topics = getTopicGroups();
        when(mockRepo.findAllByCaseType("Dept")).thenReturn(topics);

        CSVTopicLine lineOne = new CSVTopicLine("First1", "Name1", "Unit1", "Team1");
        CSVTopicLine lineTwo = new CSVTopicLine("First2", "Name2", "Unit2", "Team2");
        CSVTopicLine lineThree = new CSVTopicLine("First3", "Name3", "Unit3", "Team3");
        Set<CSVTopicLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineTwo);
        lines.add(lineThree);
        topicsService.updateTopics(lines, "Dept");

        verify(mockRepo, times(1)).save(anyList());
    }

    @Test
    public void testServiceUpdateTopicsFromCsvAddWhenAlreadyDeleted() {
        Set<TopicGroup> topics = getTopicGroupsChildDeleted(true, true);
        when(mockRepo.findAllByCaseType("Dept")).thenReturn(topics);

        CSVTopicLine lineOne = new CSVTopicLine("First1", "Name1", "Unit1", "Team1");
        CSVTopicLine lineTwo = new CSVTopicLine("First2", "Name2", "Unit2", "Team2");
        Set<CSVTopicLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineTwo);
        topicsService.updateTopics(lines, "Dept");

        verify(mockRepo).save(captor.capture());
        final Set<TopicGroup> topicListGroup = captor.getValue();

        verify(mockRepo, times(1)).save(anyList());
        assertThat(topicListGroup).isNotNull();
        assertThat(topicListGroup).hasSize(2);

        TopicGroup topicGroup1 = getTopicGroupByName(topicListGroup,"First1");
        assertThat(topicGroup1).isNotNull();
        assertThat(topicGroup1.getDeleted()).isFalse();

        Topic topic = getTopicItemByName(topicGroup1.getTopicListItems(), "Name1");
        assertThat(topic).isNotNull();
        assertThat(topic.getDeleted()).isFalse();

        TopicGroup topicGroup2 = getTopicGroupByName(topicListGroup,"First2");
        assertThat(topicGroup2).isNotNull();
        assertThat(topicGroup2.getDeleted()).isFalse();

        Topic topic2 = getTopicItemByName(topicGroup2.getTopicListItems(), "Name2");
        assertThat(topic2).isNotNull();
        assertThat(topic2.getDeleted()).isFalse();
    }

    @Test
    public void testServiceUpdateTopicsFromCsvEmptyGroupIsDeleted() {
        Set<TopicGroup> topics = getTopicGroupsChildDeleted(true, true);
        when(mockRepo.findAllByCaseType("Dept")).thenReturn(topics);

        CSVTopicLine lineOne = new CSVTopicLine("First1", "Name4", "Unit1", "Team1");
        Set<CSVTopicLine> lines = new HashSet<>();
        lines.add(lineOne);
        topicsService.updateTopics(lines, "Dept");

        verify(mockRepo).save(captor.capture());
        final Set<TopicGroup> topicListGroup = captor.getValue();

        verify(mockRepo, times(1)).save(anyList());
        assertThat(topicListGroup).isNotNull();
        assertThat(topicListGroup).hasSize(1);

        TopicGroup topicGroup1 = getTopicGroupByName(topicListGroup,"First1");
        assertThat(topicGroup1).isNotNull();
        assertThat(topicGroup1.getDeleted()).isFalse();

        Topic topic = getTopicItemByName(topicGroup1.getTopicListItems(), "Name1");
        assertThat(topic).isNotNull();
        assertThat(topic.getDeleted()).isTrue();

        Topic topic2 = getTopicItemByName(topicGroup1.getTopicListItems(), "Name4");
        assertThat(topic2).isNotNull();
        assertThat(topic2.getDeleted()).isFalse();
    }

    @Test
    public void testServiceUpdateTopicsFromCSVRemove() throws ListNotFoundException {
        Set<TopicGroup> topics = getTopicGroups();
        when(mockRepo.findAllByCaseType("Dept")).thenReturn(topics);

        CSVTopicLine lineOne = new CSVTopicLine("First1", "Name1", "Unit1", "Team1");
        Set<CSVTopicLine> lines = new HashSet<>();
        lines.add(lineOne);
        topicsService.updateTopics(lines, "Dept");

        verify(mockRepo).save(captor.capture());
        final Set<TopicGroup> topicListGroup = captor.getValue();

        verify(mockRepo, times(1)).save(anyList());
        assertThat(topicListGroup).isNotNull();
        assertThat(topicListGroup).hasSize(2);

        TopicGroup topicGroup1 = getTopicGroupByName(topicListGroup,"First1");
        assertThat(topicGroup1).isNotNull();
        assertThat(topicGroup1.getDeleted()).isFalse();

        Topic topic = getTopicItemByName(topicGroup1.getTopicListItems(), "Name1");
        assertThat(topic).isNotNull();
        assertThat(topic.getDeleted()).isFalse();

        TopicGroup topicGroup2 = getTopicGroupByName(topicListGroup,"First2");
        assertThat(topicGroup2).isNotNull();
        assertThat(topicGroup2.getDeleted()).isTrue();

        Topic topic2 = getTopicItemByName(topicGroup2.getTopicListItems(), "Name2");
        assertThat(topic2).isNotNull();
        assertThat(topic2.getDeleted()).isTrue();

    }

    @Test
    public void testServiceUpdateTopicsFromCSVRemoveChildTopic() throws ListNotFoundException {
        Set<TopicGroup> topics = getTopicGroups();
        when(mockRepo.findAllByCaseType("Dept")).thenReturn(topics);

        CSVTopicLine lineOne = new CSVTopicLine("First1", "Name1", "Unit1", "Team1");
        CSVTopicLine lineTwo = new CSVTopicLine("First2", "Name2", "Unit2", "Team2");
        Set<CSVTopicLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineTwo);
        topicsService.updateTopics(lines, "Dept");

        verify(mockRepo).save(captor.capture());
        final Set<TopicGroup> topicListGroup = captor.getValue();

        verify(mockRepo, times(1)).save(anyList());
        assertThat(topicListGroup).isNotNull();
        assertThat(topicListGroup).hasSize(2);

        TopicGroup topicGroup1 = getTopicGroupByName(topicListGroup,"First1");
        assertThat(topicGroup1).isNotNull();
        assertThat(topicGroup1.getDeleted()).isFalse();

        Topic topic = getTopicItemByName(topicGroup1.getTopicListItems(), "Name1");
        assertThat(topic).isNotNull();
        assertThat(topic.getDeleted()).isFalse();

        TopicGroup topicGroup2 = getTopicGroupByName(topicListGroup,"First2");
        assertThat(topicGroup2).isNotNull();
        assertThat(topicGroup2.getDeleted()).isFalse();

        Topic topic2 = getTopicItemByName(topicGroup2.getTopicListItems(), "Name2");
        assertThat(topic2).isNotNull();
        assertThat(topic2.getDeleted()).isFalse();

        Topic topic3 = getTopicItemByName(topicGroup2.getTopicListItems(), "Name3");
        assertThat(topic3).isNotNull();
        assertThat(topic3.getDeleted()).isTrue();
    }

    @Test
    public void testServiceUpdateTopicsFromCSVBoth() throws ListNotFoundException {
        Set<TopicGroup> topics = getTopicGroups();
        when(mockRepo.findAllByCaseType("Dept")).thenReturn(topics);

        CSVTopicLine lineOne = new CSVTopicLine("First1", "Name1", "Unit1", "Team1");
        CSVTopicLine lineThree = new CSVTopicLine("First2", "Name3", "Unit3", "Team3");
        CSVTopicLine lineFour = new CSVTopicLine("First2", "Name4", "Unit4", "Team4");
        Set<CSVTopicLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineThree);
        lines.add(lineFour);
        topicsService.updateTopics(lines, "Dept");

        verify(mockRepo, times(1)).save(anyList());
    }

    @Test
    public void testServiceUpdateTopicsFromCSVSame() throws ListNotFoundException {
        Set<TopicGroup> topics = getTopicGroups();
        when(mockRepo.findAllByCaseType("Dept")).thenReturn(topics);

        CSVTopicLine lineOne = new CSVTopicLine("First1", "Name1", "Unit1", "Team1");
        CSVTopicLine lineTwo = new CSVTopicLine("First2", "Name2", "Unit2", "Team2");
        Set<CSVTopicLine> lines = new HashSet<>();
        lines.add(lineOne);
        lines.add(lineTwo);
        topicsService.updateTopics(lines, "Dept");

        verify(mockRepo, times(1)).save(anyList());
    }

    @Test
    public void testServiceUpdateTopicsFromCSVNothingNone() throws ListNotFoundException {
        Set<TopicGroup> topics = getTopicGroups();
        when(mockRepo.findAllByCaseType("Dept")).thenReturn(topics);

        Set<CSVTopicLine> lines = new HashSet<>();
        topicsService.updateTopics(lines, "Dept");

        verify(mockRepo, times(1)).save(anyList());
    }

    private static Set<TopicGroup> getTopicGroups() {
        Set<TopicGroup> topics = new HashSet<>();

        Topic topicOne = new Topic("Name1", "Unit1", "Team1");
        Set<Topic> topicSetOne = new HashSet<>();
        topicSetOne.add(topicOne);
        TopicGroup topicGroupOne = new TopicGroup("First1", "Dept");
        topicGroupOne.setTopicListItems(topicSetOne);

        Topic topicTwo = new Topic("Name2", "Unit2", "Team2");
        Topic topicThree = new Topic("Name3", "Unit3", "Team3");
        Set<Topic> topicSetTwo = new HashSet<>();
        topicSetTwo.add(topicTwo);
        topicSetTwo.add(topicThree);
        TopicGroup topicGroupTwo = new TopicGroup("First2", "Dept");
        topicGroupTwo.setTopicListItems(topicSetTwo);

        topics.add(topicGroupOne);
        topics.add(topicGroupTwo);
        return topics;
    }

    private static Set<TopicGroup> getTopicGroupsChildDeleted(Boolean parent, Boolean child) {
        Set<TopicGroup> topics = new HashSet<>();

        Topic topicOne = new Topic("Name1", "Unit1", "Team1");
        topicOne.setDeleted(child);
        Set<Topic> topicSetOne = new HashSet<>();
        topicSetOne.add(topicOne);
        TopicGroup topicGroupOne = new TopicGroup("First1", "Dept");
        topicGroupOne.setTopicListItems(topicSetOne);
        topicGroupOne.setDeleted(parent);

        topics.add(topicGroupOne);
        return topics;
    }

    private static Set<CSVTopicLine> buildValidCSVTopicLines() {
        Set<CSVTopicLine> lines = new HashSet<>();

        CSVTopicLine line = new CSVTopicLine("ParentTopicName", "TopicName", "TopicUnit", "TopicTeam");
        lines.add(line);

        return lines;
    }

    private static Set<TopicGroup> buildTopicList() {
        TopicGroup topicGroup = new TopicGroup("TopicName", "CaseType");

        Set<Topic> topics = new HashSet<>();
        topics.add(new Topic("TopicName", "OwningUnit", "OwningTeam"));
        topicGroup.setTopicListItems(topics);

        Set<TopicGroup> records = new HashSet<>();
        records.add(topicGroup);

        return records;
    }

    private static TopicGroup getTopicGroupByName(Set<TopicGroup> topicGroups, String topicGroupName)
    {
        return topicGroups.stream().filter(t -> t.getName().equals(topicGroupName)).findFirst().orElse(null);
    }

    private static Topic getTopicItemByName(Set<Topic> topics, String topicName)
    {
        return topics.stream().filter(t -> t.getName().equals(topicName)).findFirst().orElseGet(null);
    }


}