package uk.gov.digital.ho.hocs.dto.topics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.TopicGroup;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class TopicGroupRecord {

    private String name;

    private String caseType;

    private List<TopicRecord> topicListItems;

    public static TopicGroupRecord create(TopicGroup groupList) {
        return create(groupList, false);
    }

    public static TopicGroupRecord create(TopicGroup groupList, boolean showDeleted) {
        List<TopicRecord> topicList = groupList.getTopicListItems().stream().filter(topic -> !topic.getDeleted() || showDeleted).map(TopicRecord::create).collect(Collectors.toList());
        return new TopicGroupRecord(groupList.getName(), groupList.getCaseType(),topicList);
    }
}