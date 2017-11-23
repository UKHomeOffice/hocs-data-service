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
import uk.gov.digital.ho.hocs.dto.legacy.users.UserCreateRecord;
import uk.gov.digital.ho.hocs.dto.legacy.users.UserRecord;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.legacy.users.CSVUserLine;
import uk.gov.digital.ho.hocs.model.BusinessGroup;
import uk.gov.digital.ho.hocs.model.User;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
        Set<User> users = userRepository.findAllByDepartment(departmentRef);
        if(users.isEmpty())
        {
            throw new ListNotFoundException();
        }
        return UserCreateRecord.create(users);
    }

    @Cacheable(value = "usersByGroupName", key = "#groupRef")
    public UserRecord getUsersByGroupName(String groupRef) throws ListNotFoundException {
        Set<User> users = userRepository.findAllByBusinessGroupReference(groupRef);
        if(users.isEmpty())
        {
            throw new ListNotFoundException();
        }
        return UserRecord.create(users);
    }

    @Caching( evict = {@CacheEvict(value = "usersByDeptName", allEntries = true),
                       @CacheEvict(value = "usersByGroupName", allEntries = true)})
    public void createUsersFromCSV(Set<CSVUserLine> lines, String department) throws ListNotFoundException{
        Set<User> users = getUsers(lines, department);
        if(!users.isEmpty()) {
            createUsers(users);
        }
    }

    @Transactional
    @Caching( evict = {@CacheEvict(value = "usersByDeptName", allEntries = true),
                       @CacheEvict(value = "usersByGroupName", allEntries = true)})
    public void updateUsersByDepartment(Set<CSVUserLine> lines,String department) throws ListNotFoundException {
        Set<User> users = getUsers(lines, department);
        Set<User> jpaUsers = userRepository.findAllByDepartment(department);

        // Get list of users to remove
        Set<User> usersToDelete = jpaUsers.stream().filter(user -> !users.contains(user)).collect(Collectors.toSet());

        // Get list of users to add
        Set<User> usersToAdd = users.stream().filter(user -> !jpaUsers.contains(user)).collect(Collectors.toSet());

        if(!usersToDelete.isEmpty()) {
            deleteUsers(usersToDelete);
        }
        if(!usersToAdd.isEmpty()) {
            createUsers(usersToAdd);
        }
    }

    private Set<User> getUsers(Set<CSVUserLine> lines, String department) throws ListNotFoundException {
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
        return users;
    }

    private void deleteUsers(Set<User> users) {
        userRepository.delete(users);
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