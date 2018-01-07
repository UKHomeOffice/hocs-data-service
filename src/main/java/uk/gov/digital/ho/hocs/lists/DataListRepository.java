package uk.gov.digital.ho.hocs.lists;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.lists.model.DataList;

import java.util.Set;

@Repository
public interface DataListRepository extends CrudRepository<DataList, Long> {

    DataList findOneByName(String name);
    DataList findOneByNameAndDeletedIsFalse(String name);

    Set<DataList> findAllByDeletedIsFalse();

}
