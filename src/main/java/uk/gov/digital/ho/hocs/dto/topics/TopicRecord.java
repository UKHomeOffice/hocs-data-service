package uk.gov.digital.ho.hocs.dto.topics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.Topic;

@AllArgsConstructor
@Getter
public class TopicRecord {

    private String topicName;

    private String topicUnit;

    private String topicTeam;

    public static TopicRecord create(Topic topic) {

        return new TopicRecord(topic.getName(), topic.getTopicUnit(), topic.getTopicTeam());
    }

}