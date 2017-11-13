package uk.gov.digital.ho.hocs;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.model.DataList;

@Repository
public interface DataListRepository extends CrudRepository<DataList, Long> {

    DataList findOneByName(String name);

    DataList findDataListByName(String name);

}
