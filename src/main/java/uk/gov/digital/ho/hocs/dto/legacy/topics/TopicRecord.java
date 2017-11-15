package uk.gov.digital.ho.hocs.dto.legacy.topics;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.DataListEntityProperty;
import uk.gov.digital.ho.hocs.model.Topic;
import uk.gov.digital.ho.hocs.model.TopicGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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