package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.dto.legacy.topics.TopicGroupRecord;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.ingest.topics.CSVTopicLine;
import uk.gov.digital.ho.hocs.model.Topic;
import uk.gov.digital.ho.hocs.model.TopicGroup;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TopicsService {

    private final TopicsRepository repo;

    @Autowired
    public TopicsService(TopicsRepository repo) {
        this.repo = repo;
    }

    @Cacheable(value = "topics", key = "#caseType")
    public List<TopicGroupRecord> getTopicByCaseType(String caseType) throws ListNotFoundException {
        Set<TopicGroup> list = repo.findAllByCaseTypeAndDeletedIsFalse(caseType);
        if (list.isEmpty()) {
            throw new ListNotFoundException();
        }
        return list.stream().map(TopicGroupRecord::create).collect(Collectors.toList());
    }

    @Cacheable(value = "topics", key = "all")
    public List<TopicGroupRecord> getAllTopics() throws ListNotFoundException {
        Set<TopicGroup> list = repo.findAllByDeletedIsFalse();
        if (list.isEmpty()) {
            throw new ListNotFoundException();
        }
        return list.stream().map(TopicGroupRecord::create).collect(Collectors.toList());
    }

    @Transactional
    @Caching( evict = {@CacheEvict(value = "topics", key = "#caseType"),
                       @CacheEvict(value = "topics", key = "all")})
    public void updateTopics(Set<CSVTopicLine> lines, String caseType) {
        List<TopicGroup> newTopics = buildTopicGroups(lines, caseType);
        Set<TopicGroup> jpaTopics = repo.findAllByCaseType(caseType);

        // Update existing topic groups
        jpaTopics.forEach(jpaTopic -> {

            if(newTopics.contains(jpaTopic)) {
                TopicGroup matchingNewTopicGroup = newTopics.get(newTopics.indexOf(jpaTopic));

                Set<Topic> newTopicListItems = matchingNewTopicGroup.getTopicListItems();
                Set<Topic> jpaTopicListItems = jpaTopic.getTopicListItems();


                // Update existing topic items
                jpaTopicListItems.forEach(item -> {
                        item.setDeleted(!newTopicListItems.contains(item));
                });

                // Add new topic items
                newTopicListItems.forEach(newTopic -> {
                    if(!jpaTopicListItems.contains(newTopic))
                    {
                        jpaTopicListItems.add(newTopic);
                    }
                });

                jpaTopic.setTopicListItems(jpaTopicListItems);

                // Set the topic group to deleted if there are no visible topic items
                jpaTopic.setDeleted(jpaTopic.getTopicListItems().stream().allMatch(topic -> topic.getDeleted()));
            }
            else {
                jpaTopic.getTopicListItems().forEach(item -> item.setDeleted(true));
                jpaTopic.setDeleted(true);
            }
        });

        // Add new topic groups
        newTopics.forEach(newTopicGroup -> {
            if (!jpaTopics.contains(newTopicGroup)) {
                jpaTopics.add(newTopicGroup);
            }
        });

        saveTopicGroups(jpaTopics);
    }

    @CacheEvict(value = "topics", allEntries = true)
    public void clearCache(){
        log.info("All topics cache cleared");
    }

    private static List<TopicGroup> buildTopicGroups(Set<CSVTopicLine> lines, String caseType) {
        // Build a Map of parent topic strings Strings to sub topics
        Map<String, Set<Topic>> topics = new HashMap<>();
        for (CSVTopicLine line : lines) {
            topics.putIfAbsent(line.getParentTopicName(), new HashSet<>());
            Topic topic = new Topic(line.getTopicName(), line.getTopicUnit(), line.getTopicTeam());
            topics.get(line.getParentTopicName()).add(topic);
        }

        // Build a set of topic groups based on each entry of parent String and subtopics
        List<TopicGroup> topicGroups = new ArrayList<>();
        for (Map.Entry<String, Set<Topic>> entity : topics.entrySet()) {
            String parentTopicName = entity.getKey();
            TopicGroup topicGroup = new TopicGroup(parentTopicName, caseType);
            topicGroup.setTopicListItems(entity.getValue());
            topicGroups.add(topicGroup);
        }
        return topicGroups;
    }

    private void saveTopicGroups(Set<TopicGroup> topicGroups) {
        try {
            if(topicGroups.size() > 0) {
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
