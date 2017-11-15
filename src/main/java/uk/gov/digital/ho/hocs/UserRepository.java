package uk.gov.digital.ho.hocs;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.model.User;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    List<User> findAllByDepartment(String department);

    @Query("select u from User u " +
            "join u.groups g " +
            "where g.referenceName = :refName")
    List<User> findAllByBusinessGroupReference(@Param("refName") String refName);
}