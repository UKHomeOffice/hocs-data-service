package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.model.DataList;
import uk.gov.digital.ho.hocs.model.DataListEntity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataListService {
    private final DataListRepository repo;

    @Autowired
    public DataListService(DataListRepository repo) {
        this.repo = repo;
    }

    @Cacheable(value = "list", key = "#name")
    public DataList getDataListByName(String name) throws ListNotFoundException {
       DataList list = repo.findOneByNameAndDeletedIsFalse(name);
        if(list == null) {
            throw new ListNotFoundException();
        }
        return list;
    }

    @Cacheable(value = "list")
    public Set<DataList> getAllDataLists() throws ListNotFoundException {
        Set<DataList> list = repo.findAllByDeletedIsFalse();
        if (list.isEmpty()) {
            throw new ListNotFoundException();
        }
        return list;
    }

    @Caching( evict = {@CacheEvict(value = "list", key = "#newDataList.name"),
                       @CacheEvict(value = "list")})
    public void updateDataList(DataList newDataList) {
        if(newDataList != null) {
            DataList jpaDataList = repo.findOneByName(newDataList.getName());

            // Update existing list
            if (jpaDataList != null) {
                List<DataListEntity> newEntities = newDataList.getEntities().stream().collect(Collectors.toList());
                Set<DataListEntity> jpaEntities = jpaDataList.getEntities();

                jpaEntities.forEach(item -> {
                    item.setDeleted(!newEntities.contains(item));
                });

                // Add new list items
                newEntities.forEach(newTopic -> {
                    if (!jpaEntities.contains(newTopic)) {
                        jpaEntities.add(newTopic);
                    }
                });

                jpaDataList.setEntities(jpaEntities);

                // Set the data list to deleted if there are no visible entities
                jpaDataList.setDeleted(jpaDataList.getEntities().stream().allMatch(entities -> entities.getDeleted()));
            } else {
                jpaDataList = newDataList;
            }

            saveList(jpaDataList);
        } else{
            throw new EntityCreationException("Unable to update entity");
        }
    }

    @CacheEvict(value = "list", allEntries = true)
    public void clearCache(){
        log.info("All lists cache cleared");
    }

    private void saveList(DataList dataList) throws EntityCreationException {
        try {
            if(dataList != null && dataList.getName() != null)
            repo.save(dataList);
        } catch (DataIntegrityViolationException e) {

            if (e.getCause() instanceof ConstraintViolationException &&
                    ((ConstraintViolationException) e.getCause()).getConstraintName().toLowerCase().contains("list_name_idempotent") ||
                    ((ConstraintViolationException) e.getCause()).getConstraintName().toLowerCase().contains("entity_name_ref_idempotent") ||
                    ((ConstraintViolationException) e.getCause()).getConstraintName().toLowerCase().contains("entity_id_idempotent")) {
                throw new EntityCreationException("Identified an attempt to recreate existing entity, rolling back");
            }

            throw e;
        }
    }
}