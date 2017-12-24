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
import uk.gov.digital.ho.hocs.dto.legacy.topics.TopicGroupRecord;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.ingest.topics.CSVTopicLine;
import uk.gov.digital.ho.hocs.model.Topic;
import uk.gov.digital.ho.hocs.model.TopicGroup;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        when(mockRepo.findAllByCaseTypeAndDeletedIsFalse(CASETYPE)).thenReturn(buildTopicList());

        List<TopicGroupRecord> records = topicsService.getTopicByCaseType(CASETYPE);

        verify(mockRepo).findAllByCaseTypeAndDeletedIsFalse(CASETYPE);

        assertThat(records).isNotNull();
        assertThat(records).hasOnlyElementsOfType(TopicGroupRecord.class);
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getName()).isEqualTo("TopicName");
        assertThat(records.get(0).getCaseType()).isEqualTo("CaseType");
    }

    @Test(expected = ListNotFoundException.class)
    public void testLegacyListNotFoundThrowsListNotFoundException() throws ListNotFoundException {

        List<TopicGroupRecord> records = topicsService.getTopicByCaseType(UNAVAILABLE_RESOURCE);
        verify(mockRepo).findAllByCaseType(UNAVAILABLE_RESOURCE);
        assertThat(records).isEmpty();
    }

    @Test
    public void testCreateList() {
        topicsService.createTopics(buildValidCSVTopicLines(), CASETYPE);
        verify(mockRepo).save(anyList());
    }

    @Test
    public void testCreateListNoEntities() {
        topicsService.createTopics(new HashSet<>(), CASETYPE);
        verify(mockRepo, times(0)).save(anyList());
    }

    @Test(expected = EntityCreationException.class)
    public void testRepoDataIntegrityExceptionThrowsEntityCreationException() {

        Set<CSVTopicLine> topics = buildValidCSVTopicLines();

        when(mockRepo.save(anyList())).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "topic_group_name_idempotent")));
        topicsService.createTopics(topics, CASETYPE);

        verify(mockRepo).save(anyList());
    }

    @Test(expected = EntityCreationException.class)
    public void testRepoDataIntegrityExceptionThrowsEntityCreationExceptionTwo() {

        Set<CSVTopicLine> topics = buildValidCSVTopicLines();

        when(mockRepo.save(anyList())).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "topic_name_ref_idempotent")));
        topicsService.createTopics(topics, CASETYPE);

        verify(mockRepo).save(anyList());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testRepoDataIntegrityExceptionThrowsDataIntegrityViolationException() {

        Set<CSVTopicLine> topics = buildValidCSVTopicLines();

        when(mockRepo.save(anyList())).thenThrow(new DataIntegrityViolationException("Thrown DataIntegrityViolationException", new ConstraintViolationException("", null, "")));
        topicsService.createTopics(topics, CASETYPE);

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
        final Set<TopicGroup> topicGroupList = captor.getValue();

        verify(mockRepo, times(1)).save(anyList());
        assertThat(topicGroupList).isNotNull();
        assertThat(topicGroupList).hasSize(2);

        Optional<TopicGroup> topicGroup = topicGroupList.stream().filter(t -> t.getName().equals("First1")).findFirst();
        TopicGroup topicGroupToTest = topicGroup.orElse(null);

        Optional<Topic> topic = topicGroupToTest.getTopicListItems().stream().filter(t -> t.getName().equals("Name1")).findFirst();
        Topic subTopic = topic.orElse(null);
        assertThat(subTopic).isNotNull();
        assertThat(subTopic.getDeleted()).isFalse();

        topicGroup = topicGroupList.stream().filter(t -> t.getName().equals("First2")).findFirst();
        topicGroupToTest = topicGroup.orElse(null);

        topic = topicGroupToTest.getTopicListItems().stream().filter(t -> t.getName().equals("Name2")).findFirst();
        subTopic = topic.orElse(null);
        assertThat(subTopic).isNotNull();
        assertThat(subTopic.getDeleted()).isFalse();
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
        final Set<TopicGroup> topicGroupList = captor.getValue();

        verify(mockRepo, times(1)).save(anyList());
        assertThat(topicGroupList).isNotNull();
        assertThat(topicGroupList).hasSize(2);

        Optional<TopicGroup> topicGroup = topicGroupList.stream().filter(t -> t.getName().equals("First1")).findFirst();
        TopicGroup topicGroupToTest = topicGroup.orElse(null);

        assertThat(topicGroupToTest).isNotNull();
        assertThat(topicGroupToTest.getDeleted()).isFalse();

        Optional<Topic> topic = topicGroupToTest.getTopicListItems().stream().filter(t -> t.getName().equals("Name1")).findFirst();
        Topic subTopic = topic.orElse(null);
        assertThat(subTopic).isNotNull();
        assertThat(subTopic.getDeleted()).isFalse();

        topicGroup = topicGroupList.stream().filter(t -> t.getName().equals("First2")).findFirst();
        topicGroupToTest = topicGroup.orElse(null);

        assertThat(topicGroupToTest).isNotNull();
        assertThat(topicGroupToTest.getDeleted()).isTrue();

        topic = topicGroupToTest.getTopicListItems().stream().filter(t -> t.getName().equals("Name2")).findFirst();
        subTopic = topic.orElse(null);
        assertThat(subTopic).isNotNull();
        assertThat(subTopic.getDeleted()).isTrue();

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
        final Set<TopicGroup> topicGroupList = captor.getValue();

        verify(mockRepo, times(1)).save(anyList());
        assertThat(topicGroupList).isNotNull();
        assertThat(topicGroupList).hasSize(2);

        Optional<TopicGroup> topicGroup = topicGroupList.stream().filter(t -> t.getName().equals("First1")).findFirst();
        TopicGroup topicGroupToTest = topicGroup.orElse(null);

        assertThat(topicGroupToTest).isNotNull();
        assertThat(topicGroupToTest.getDeleted()).isFalse();
        Optional<Topic> topic = topicGroupToTest.getTopicListItems().stream().filter(t -> t.getName().equals("Name1")).findFirst();
        Topic subTopic = topic.orElse(null);
        assertThat(subTopic).isNotNull();
        assertThat(subTopic.getDeleted()).isFalse();

        topicGroup = topicGroupList.stream().filter(t -> t.getName().equals("First2")).findFirst();
        topicGroupToTest = topicGroup.orElse(null);

        assertThat(topicGroupToTest).isNotNull();
        assertThat(topicGroupToTest.getDeleted()).isFalse();

        topic = topicGroupToTest.getTopicListItems().stream().filter(t -> t.getName().equals("Name2")).findFirst();
        subTopic = topic.orElse(null);
        assertThat(subTopic).isNotNull();
        assertThat(subTopic.getDeleted()).isFalse();

        topic = topicGroupToTest.getTopicListItems().stream().filter(t -> t.getName().equals("Name3")).findFirst();
        subTopic = topic.orElse(null);
        assertThat(subTopic).isNotNull();
        assertThat(subTopic.getDeleted()).isTrue();


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
        topicsService.updateTopics(lines, "Dept");

        verify(mockRepo, times(1)).save(anyList());
    }

    @Test
    public void testServiceUpdateTopicsFromCSVNothingSame() throws ListNotFoundException {
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

    private Set<TopicGroup> getTopicGroups() {
        Set<TopicGroup> topics = new HashSet<>();

        Topic topicOne = new Topic("Name1", "Unit1", "Team1");
        Set<Topic> topicSetOne = new HashSet<>();
        topicSetOne.add(topicOne);
        TopicGroup topicGroupOne = new TopicGroup("First1", "Dept");
        topicGroupOne.setTopicListItems(topicSetOne);

        Topic topicTwo = new Topic("Name2", "Unit2", "Team2");
        Set<Topic> topicSetTwo = new HashSet<>();
        topicSetTwo.add(topicTwo);
        TopicGroup topicGroupTwo = new TopicGroup("First2", "Dept");
        topicGroupTwo.setTopicListItems(topicSetTwo);

        Topic topicThree = new Topic("Name3", "Unit3", "Team3");
        Set<Topic> topicSetThree = new HashSet<>();
        topicSetTwo.add(topicThree);
        TopicGroup topicGroupThree = new TopicGroup("First2", "Dept");
        topicGroupTwo.setTopicListItems(topicSetTwo);

        topics.add(topicGroupOne);
        topics.add(topicGroupTwo);
        return topics;
    }

    private Set<TopicGroup> getTopicGroupsChildDeleted(Boolean parent, Boolean child) {
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

    private Set<CSVTopicLine> buildValidCSVTopicLines() {
        Set<CSVTopicLine> lines = new HashSet<>();

        CSVTopicLine line = new CSVTopicLine("ParentTopicName", "TopicName", "TopicUnit", "TopicTeam");
        lines.add(line);

        return lines;
    }

    private Set<TopicGroup> buildTopicList() {
        TopicGroup topicGroup = new TopicGroup("TopicName", "CaseType");

        Set<Topic> topics = new HashSet<>();
        topics.add(new Topic("TopicName", "OwningUnit", "OwningTeam"));
        topicGroup.setTopicListItems(topics);

        Set<TopicGroup> records = new HashSet<>();
        records.add(topicGroup);

        return records;
    }


}