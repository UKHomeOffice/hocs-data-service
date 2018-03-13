package uk.gov.digital.ho.hocs.user;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.AlfrescoClient;
import uk.gov.digital.ho.hocs.businessGroups.BusinessUnitService;
import uk.gov.digital.ho.hocs.businessGroups.model.BusinessTeam;
import uk.gov.digital.ho.hocs.exception.AlfrescoPostException;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.user.dto.UserSetRecord;
import uk.gov.digital.ho.hocs.user.ingest.CSVUserLine;
import uk.gov.digital.ho.hocs.user.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BusinessUnitService groupService;
    private final AlfrescoClient alfrescoClient;

    @Autowired
    public UserService(UserRepository userRepository, BusinessUnitService groupService, AlfrescoClient alfrescoClient) {
        this.userRepository = userRepository;
        this.groupService = groupService;
        this.alfrescoClient = alfrescoClient;
    }

    UserSetRecord getUsersByDepartmentName(String departmentRef) throws EntityNotFoundException {
        Set<User> users = userRepository.findAllByDepartment(departmentRef);
        if(users.isEmpty()){
            throw new EntityNotFoundException();
        }
        return UserSetRecord.create(users);
    }

    void publishUsersByDepartmentName(String departmentRef) throws EntityNotFoundException, AlfrescoPostException {
        Set<User> users = userRepository.findAllByDepartment(departmentRef);
        if (users.isEmpty()) {
            throw new EntityNotFoundException();
        }
        alfrescoClient.postUsers(new ArrayList<>(users));
    }

    @Cacheable(value = "users", key = "#department")
    public UserSetRecord getUsersByGroupName(String department) throws EntityNotFoundException {
        Set<User> users = userRepository.findAllByBusinessGroupReference(department);
        if(users.isEmpty()){
            throw new EntityNotFoundException();
        }
        return UserSetRecord.create(users);
    }

    //TODO: this behaviour is wrong, we should publish the update, deleting the accounts from alfresco, should also unallocate cases too.
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void updateUsersByDepartment(Set<CSVUserLine> lines, String department) throws EntityNotFoundException {

        Set<User> users = getUsers(lines, department);
        Set<User> jpaUsers = userRepository.findAllByDepartment(department);

        /*
        Hack to make this work with alfresco, force user to delete and recreate to ensure groups are correct
         */

        // Get list of users to remove
        Set<User> usersToDelete = jpaUsers;//.stream().filter(user -> !users.contains(user)).collect(Collectors.toSet());

        // Get list of users to add
        Set<User> usersToAdd = users;// .stream().filter(user -> !jpaUsers.contains(user)).collect(Collectors.toSet());

        if(!usersToDelete.isEmpty()) {
            deleteUsers(usersToDelete);
        }
        if(!usersToAdd.isEmpty()) {
            createUsers(usersToAdd);
        }
    }

    private Set<User> getUsers(Set<CSVUserLine> lines, String department) throws EntityNotFoundException {
        Set<User> users = new HashSet<>();
        for (CSVUserLine line : lines) {
            User user = new User(line.getFirst(), line.getLast(), line.getEmail(), line.getEmail(), department);

            Set<BusinessTeam> groups = new HashSet<>();
            for(String group : line.getGroups()) {
                groups.addAll(groupService.getTeamByReference(group));
            }
            user.setTeams(groups);
            users.add(user);
        }
        return users;
    }

    private void deleteUsers(Set<User> users) {
        userRepository.deleteAll();
    }

    private void createUsers(Set<User> users) {
        try {
            userRepository.saveAll(users);
        } catch (DataIntegrityViolationException e) {

            if (e.getCause() instanceof ConstraintViolationException &&
                    ((ConstraintViolationException) e.getCause()).getConstraintName().toLowerCase().contains("user_name_idempotent")) {
                throw new EntityCreationException("Identified an attempt to recreate existing entity, rolling back");
            }

            throw e;
        }
    }

}