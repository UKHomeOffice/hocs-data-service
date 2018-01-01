package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.digital.ho.hocs.dto.users.UserCreateRecord;
import uk.gov.digital.ho.hocs.dto.users.UserRecord;
import uk.gov.digital.ho.hocs.exception.AlfrescoPostException;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.ingest.users.UserFileParser;

import java.util.List;

@RestController
@Slf4j
public class UserResource {
    private final UserService userService;

    @Autowired
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/users/{group}", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<UserRecord> putUsersByGroup(@PathVariable("group") String group, @RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            log.info("Parsing \"{}\" Users File", group);
            try {
                userService.updateUsersByDepartment(new UserFileParser(file).getLines(), group);
                return ResponseEntity.ok().build();
            } catch (EntityCreationException | ListNotFoundException e) {
                log.info("{} Users not created", group);
                log.info(e.getMessage());
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @RequestMapping(value = {"/users/{group}","s/homeoffice/cts/teamUsers"}, method = RequestMethod.GET)
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

    @RequestMapping(value = "/users/{group}/publish/", method = RequestMethod.GET)
    public ResponseEntity<List<UserCreateRecord>> postUsersToAlfresco(@PathVariable("group") String group) {
        try {
            userService.publishUsersByDepartmentName(group);
            return ResponseEntity.ok().build();

        } catch (ListNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        } catch (AlfrescoPostException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
