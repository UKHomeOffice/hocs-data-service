package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.dto.DataListEntityRecord;
import uk.gov.digital.ho.hocs.dto.DataListRecord;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.model.DataList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class DataListService {
    private final DataListRepository repo;

    @Autowired
    public DataListService(DataListRepository repo) {
        this.repo = repo;
    }

    @Cacheable(value = "list", key = "#name")
    public DataListRecord getListByName(String name) throws ListNotFoundException {
        try {
            DataList list = repo.findOneByName(name);
            return DataListRecord.create(list);
        } catch (NullPointerException e) {
            throw new ListNotFoundException();
        }
    }

    public DataListRecord getCombinedList(String name, String... lists) throws ListNotFoundException {
        try {
            List<DataListEntityRecord> listEntities = new ArrayList<>();

            for (String list : lists) {
                DataListRecord dataListRecord = getListByName(list);
                listEntities = Stream
                        .concat(listEntities.stream(), dataListRecord.getEntities().stream())
                        .collect(Collectors.toList());
            }
            return new DataListRecord(name, listEntities);
        } catch (ListNotFoundException e) {
            throw new ListNotFoundException();
        }
    }

    @CacheEvict(value = "list", key = "#dataList.getName()")
    public void createList(DataList dataList) throws EntityCreationException {
        try {
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