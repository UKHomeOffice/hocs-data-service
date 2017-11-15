package uk.gov.digital.ho.hocs.dto.legacy.topics;

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
        List<TopicRecord> topicList = groupList.getTopicListItems().stream().map(TopicRecord::create).collect(Collectors.toList());
        return new TopicGroupRecord(groupList.getName(), groupList.getCaseType(),topicList);
    }
}