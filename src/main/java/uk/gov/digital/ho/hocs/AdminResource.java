package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.dto.legacy.users.UserCreateRecord;
import uk.gov.digital.ho.hocs.exception.AlfrescoPostException;
import uk.gov.digital.ho.hocs.exception.IngestException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;

import java.util.List;

@RestController
@Slf4j
public class AdminResource {
    private final AdminService adminService;

    @Autowired
    public AdminResource(AdminService adminService) {
        this.adminService = adminService;
    }

    @RequestMapping(value = "admin/cache/clear", method = RequestMethod.GET)
    public ResponseEntity clearCache() {
        adminService.clearCache();
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "admin/members/refresh", method = RequestMethod.GET)
    public ResponseEntity getFromApi() {
        try {
            adminService.updateWebMemberLists();
            return ResponseEntity.ok().build();
        } catch (IngestException e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().build();
    }

    @RequestMapping(value = "admin/users/{group}/publish/", method = RequestMethod.GET)
    public ResponseEntity<List<UserCreateRecord>> postUsersToAlfresco(@PathVariable("group") String group) {
        try {
            adminService.publishUsersByDepartmentName(group);
            return ResponseEntity.ok().build();

        } catch (ListNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        } catch (AlfrescoPostException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }



}
