package uk.gov.digital.ho.hocs.topics.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.topics.model.Topic;
import uk.gov.digital.ho.hocs.topics.model.TopicGroup;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TopicGroupRecordTest {

    @Test
    public void createWithEntities() throws Exception {
        TopicGroup topicGroup = new TopicGroup("TopicName", "CaseType");

        Set<Topic> topics = new HashSet<>();
        topics.add(new Topic("TopicName", "OwningUnit","OwningTeam"));
        topicGroup.setTopicListItems(topics);

        TopicGroupRecord record = TopicGroupRecord.create(topicGroup);

        assertThat(record.getName()).isEqualTo("TopicName");
        assertThat(record.getCaseType()).isEqualTo("CaseType");
        assertThat(record.getTopicListItems()).hasSize(1);
    }

    @Test
    public void createWithoutEntities() throws Exception {
        TopicGroup topicGroup = new TopicGroup("TopicName", "CaseType");

        TopicGroupRecord record = TopicGroupRecord.create(topicGroup);

        assertThat(record.getTopicListItems()).hasSize(0);
    }

}