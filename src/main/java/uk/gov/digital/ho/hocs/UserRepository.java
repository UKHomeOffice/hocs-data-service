package uk.gov.digital.ho.hocs;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.model.User;

import java.util.Set;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Set<User> findAllByDepartment(String department);

    @Query("select u from User u " +
            "join u.groups g " +
            "where g.referenceName = :refName")
    Set<User> findAllByBusinessGroupReference(@Param("refName") String refName);
}