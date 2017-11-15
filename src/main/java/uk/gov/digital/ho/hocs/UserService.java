package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.dto.legacy.users.UserCreateRecord;
import uk.gov.digital.ho.hocs.dto.legacy.users.UserRecord;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.legacy.users.CSVUserLine;
import uk.gov.digital.ho.hocs.model.BusinessGroup;
import uk.gov.digital.ho.hocs.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BusinessGroupService groupService;

    @Autowired
    public UserService(UserRepository userRepository, BusinessGroupService groupService) {
        this.userRepository = userRepository;
        this.groupService = groupService;
    }

    @Cacheable(value = "usersByDeptName", key = "#departmentRef")
    public UserCreateRecord getUsersByDepartmentName(String departmentRef) throws ListNotFoundException {
        List<User> list = userRepository.findAllByDepartment(departmentRef);
        if(list.size() == 0)
        {
            throw new ListNotFoundException();
        }
        return UserCreateRecord.create(list);
    }

    @Cacheable(value = "usersByGroupName", key = "#groupRef")
    public UserRecord getUsersByGroupName(String groupRef) throws ListNotFoundException {
        List<User> list = userRepository.findAllByBusinessGroupReference(groupRef);
        if(list.size() == 0)
        {
            throw new ListNotFoundException();
        }
        return UserRecord.create(list);
    }

    @Caching( evict = {@CacheEvict(value = "usersByDeptName", allEntries = true),
                       @CacheEvict(value = "usersByGroupName", allEntries = true)})
    public void createUsersFromCSV(Set<CSVUserLine> lines, String department) throws ListNotFoundException{

        Set<User> users = new HashSet<>();
        for (CSVUserLine line : lines) {
            User user = new User(line.getFirst(), line.getLast(), line.getEmail(), line.getEmail(), department);

            Set<BusinessGroup> groups = new HashSet<>();
            for(String group : line.getGroups()) {
                groups.add(groupService.getGroupByReference(group));
            }
            user.setGroups(groups);
            users.add(user);
        }
        if(users.size() > 0) {
            createUsers(users);
        }
    }

    private void createUsers(Set<User> users) {
        try {
            userRepository.save(users);
        } catch (DataIntegrityViolationException e) {

            if (e.getCause() instanceof ConstraintViolationException &&
                    ((ConstraintViolationException) e.getCause()).getConstraintName().toLowerCase().contains("user_name_idempotent")) {
                throw new EntityCreationException("Identified an attempt to recreate existing entity, rolling back");
            }

            throw e;
        }
    }
}