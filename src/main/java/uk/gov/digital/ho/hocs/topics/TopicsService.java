package uk.gov.digital.ho.hocs.topics;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.topics.ingest.CSVTopicLine;
import uk.gov.digital.ho.hocs.topics.model.Topic;
import uk.gov.digital.ho.hocs.topics.model.TopicGroup;

import java.util.*;

@Service
@Slf4j
public class TopicsService {

    private final TopicsRepository repo;

    @Autowired
    public TopicsService(TopicsRepository repo) {
        this.repo = repo;
    }

    @Cacheable(value = "topics", key = "#caseType")
    public Set<TopicGroup> getTopicByCaseType(String caseType) throws EntityNotFoundException {
        Set<TopicGroup> list = repo.findAllByCaseTypeAndDeletedIsFalse(caseType);
        if (list.isEmpty()) {
            throw new EntityNotFoundException();
        }
        return list;
    }

    @Cacheable(value = "topics")
    public Set<TopicGroup> getAllTopics() {
        return repo.findAllByDeletedIsFalse();
    }

    @CacheEvict(value = "topics", allEntries = true)
    public void updateTopics(Set<CSVTopicLine> lines, String caseType) {
       if(lines != null && caseType != null) {
           List<TopicGroup> newTopics = buildTopicGroups(lines, caseType);
           Set<TopicGroup> jpaTopics = repo.findAllByCaseType(caseType);

           // Update existing topic teams
           jpaTopics.forEach(jpaTopic -> {

               if (newTopics.contains(jpaTopic)) {
                   TopicGroup matchingNewTopicGroup = newTopics.get(newTopics.indexOf(jpaTopic));

                   Set<Topic> newTopicListItems = matchingNewTopicGroup.getTopicListItems();
                   Set<Topic> jpaTopicListItems = jpaTopic.getTopicListItems();


                   // Update existing topic items
                   jpaTopicListItems.forEach(item -> {
                       item.setDeleted(!newTopicListItems.contains(item));
                   });

                   // Add new topic items
                   newTopicListItems.forEach(newTopic -> {
                       if (!jpaTopicListItems.contains(newTopic)) {
                           jpaTopicListItems.add(newTopic);
                       }
                   });

                   jpaTopic.setTopicListItems(jpaTopicListItems);

                   // Set the topic group to deleted if there are no visible topic items
                   jpaTopic.setDeleted(jpaTopic.getTopicListItems().stream().allMatch(Topic::getDeleted));
               } else {
                   jpaTopic.getTopicListItems().forEach(item -> item.setDeleted(true));
                   jpaTopic.setDeleted(true);
               }
           });

           // Add new topic teams
           newTopics.forEach(newTopicGroup -> {
               if (!jpaTopics.contains(newTopicGroup)) {
                   jpaTopics.add(newTopicGroup);
               }
           });

           saveTopicGroups(jpaTopics);
       } else{
           throw new EntityCreationException("Unable to update entity");
       }
    }

    private static List<TopicGroup> buildTopicGroups(Set<CSVTopicLine> lines, String caseType) {
        Map<TopicGroup, Set<Topic>> topics = new HashMap<>();
        for (CSVTopicLine line : lines) {
            TopicGroup topicGroup = new TopicGroup(line.getParentTopicName(), caseType);
            topics.putIfAbsent(topicGroup, new HashSet<>());
            topics.get(topicGroup).add(new Topic(line.getTopicName(), line.getTopicUnit(), line.getTopicTeam()));
        }

        List<TopicGroup> topicGroups = new ArrayList<>();
        for (Map.Entry<TopicGroup, Set<Topic>> entity : topics.entrySet()) {
            TopicGroup topicGroup = entity.getKey();
            topicGroup.setTopicListItems(entity.getValue());
            topicGroups.add(topicGroup);
        }
        return topicGroups;
    }

    private void saveTopicGroups(Collection<TopicGroup> topicGroups) {
        try {
            if(!topicGroups.isEmpty()) {
                repo.save(topicGroups);
            }
        } catch (DataIntegrityViolationException e) {

            if (e.getCause() instanceof ConstraintViolationException &&
                    ((ConstraintViolationException) e.getCause()).getConstraintName().toLowerCase().contains("topic_group_name_idempotent") ||
                    ((ConstraintViolationException) e.getCause()).getConstraintName().toLowerCase().contains("topic_name_ref_idempotent")) {
                throw new EntityCreationException("Identified an attempt to recreate existing entity, rolling back");
            }

            throw e;
        }

    }
}
