package uk.gov.digital.ho.hocs.topics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.topics.model.TopicGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class TopicGroupRecord {

    @Getter
    private String name;

    @Getter
    private String caseType;

    @Getter
    private List<TopicRecord> topicListItems = new ArrayList<>();

    public static TopicGroupRecord create(TopicGroup groupList) {
        return create(groupList, false);
    }

    public static TopicGroupRecord create(TopicGroup groupList, boolean showDeleted) {
        List<TopicRecord> topicList = groupList.getTopicListItems().stream().filter(topic -> !topic.getDeleted() || showDeleted).map(TopicRecord::create).collect(Collectors.toList());
        return new TopicGroupRecord(groupList.getName(), groupList.getCaseType(),topicList);
    }
}