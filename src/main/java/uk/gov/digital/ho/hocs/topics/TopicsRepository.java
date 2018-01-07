package uk.gov.digital.ho.hocs.topics;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.topics.model.TopicGroup;

import java.util.Set;

@Repository
public interface TopicsRepository extends CrudRepository<TopicGroup, Long> {
    Set<TopicGroup> findAllByCaseType(String caseType);
    Set<TopicGroup> findAllByCaseTypeAndDeletedIsFalse(String caseType);

    Set<TopicGroup> findAllByDeletedIsFalse();
    }