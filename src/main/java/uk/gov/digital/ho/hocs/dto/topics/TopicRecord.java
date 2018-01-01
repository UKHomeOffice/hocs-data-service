package uk.gov.digital.ho.hocs.dto.topics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.Topic;

@AllArgsConstructor
public class TopicRecord {

    @Getter
    private String topicName;

    @Getter
    private String topicUnit;

    @Getter
    private String topicTeam;

    public static TopicRecord create(Topic topic) {
        return new TopicRecord(topic.getName(), topic.getTopicUnit(), topic.getTopicTeam());
    }

}