package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.gov.digital.ho.hocs.dto.legacy.users.UserCreateRecord;
import uk.gov.digital.ho.hocs.dto.legacy.users.UserRecord;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;

@RestController
@Slf4j
public class UserResource {
    private final UserService userService;

    @Autowired
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/users/{group}", method = RequestMethod.POST)
    public ResponseEntity postUsersByGroup(@PathVariable("group") String group, @RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            log.info("Parsing \"{}\" Users File", group);
            try {
                userService.createUsersFromCSV(file, group);
                return ResponseEntity.ok().build();
            } catch (EntityCreationException e) {
                log.info("{} Users not created", group);
                log.info(e.getMessage());
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @RequestMapping(value = "/users/{group}", method = RequestMethod.GET)
    public ResponseEntity<UserRecord> getUsersByGroup(@PathVariable String group) {
        log.info("\"{}\" requested", group);
        try {
            return ResponseEntity.ok(userService.getUsersByGroupName(group));
        } catch (ListNotFoundException e) {
            log.info("\"{}\" not found", group);
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/users/{group}", method = RequestMethod.PUT)
    public ResponseEntity<UserRecord> putUsersByGroup(@PathVariable("group") String group, @RequestParam("file") MultipartFile file) {
        throw new NotImplementedException();
    }

    //This is a create script, to be used once per new environment, maybe in the future this could just POST to alfresco directly.
    @RequestMapping(value = "/users/{group}/export", method = RequestMethod.GET)
    public ResponseEntity<UserCreateRecord> getUsersByReference(@PathVariable("group") String group) {
        log.info("export \"{}\" users requested", group);
        try {
            UserCreateRecord users = userService.getUsersByDepartmentName(group);
            return ResponseEntity.ok(users);
        } catch (ListNotFoundException e) {
            log.info("export \"{}\" users failed", group);
            return ResponseEntity.notFound().build();
        }
    }

}
