package uk.gov.digital.ho.hocs;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.dto.legacy.topics.TopicGroupRecord;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.model.Topic;
import uk.gov.digital.ho.hocs.model.TopicGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TopicsResourceTest {

    @Mock
    private DataListResource dataListResource;

    @Mock
    private TopicsService topicsService;

    private TopicsResource topicsResource;

    @Before
    public void setUp() {
        topicsResource = new TopicsResource(topicsService);
    }

    @Test
    public void shouldRetrieveAllEntities() throws IOException, JSONException, ListNotFoundException {
        TopicGroup topicGroup = new TopicGroup("TopicName", "CaseType");

        Set<Topic> topics = new HashSet<>();
        topics.add(new Topic("TopicName", "OwningUnit","OwningTeam"));
        topicGroup.setTopicListItems(topics);

        TopicGroupRecord record = TopicGroupRecord.create(topicGroup);
        List<TopicGroupRecord> records = new ArrayList<>();
        records.add(record);

        when(topicsService.getTopicByCaseType("CaseType")).thenReturn(records);
        ResponseEntity<List<TopicGroupRecord>> httpResponse = topicsResource.getTopicListByReference("CaseType");

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(httpResponse.getBody()).hasSize(1);

    }

    @Test
    public void shouldReturnNotFoundWhenException() throws ListNotFoundException {
        when(topicsService.getTopicByCaseType("CaseType")).thenThrow(new ListNotFoundException());
        ResponseEntity<List<TopicGroupRecord>> httpResponse = topicsResource.getTopicListByReference("CaseType");

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(httpResponse.getBody()).isNull();

    }

    @Test
    public void shouldRetrieveAllEntitiesLegacy() throws IOException, JSONException, ListNotFoundException {
        TopicGroup topicGroup = new TopicGroup("TopicName", "CaseType");

        Set<Topic> topics = new HashSet<>();
        topics.add(new Topic("TopicName", "OwningUnit","OwningTeam"));
        topicGroup.setTopicListItems(topics);

        TopicGroupRecord record = TopicGroupRecord.create(topicGroup);
        List<TopicGroupRecord> records = new ArrayList<>();
        records.add(record);

        when(topicsService.getAllTopics()).thenReturn(records);
        ResponseEntity<List<TopicGroupRecord>> httpResponse = topicsResource.getLegacyListByReference();

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(httpResponse.getBody()).hasSize(1);

    }

    @Test
    public void shouldReturnNotFoundWhenExceptionEntity() throws ListNotFoundException {
        when(topicsService.getAllTopics()).thenThrow(new ListNotFoundException());
        ResponseEntity<List<TopicGroupRecord>> httpResponse = topicsResource.getLegacyListByReference();

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(httpResponse.getBody()).isNull();

    }
}