package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.gov.digital.ho.hocs.dto.legacy.units.UnitCreateRecord;
import uk.gov.digital.ho.hocs.dto.legacy.units.UnitRecord;
import uk.gov.digital.ho.hocs.dto.legacy.users.UserRecord;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;

@RestController
@Slf4j
public class BusinessGroupResource {
    private final BusinessGroupService businessGroupService;

    @Autowired
    public BusinessGroupResource(BusinessGroupService businessGroupService) {
        this.businessGroupService = businessGroupService;
    }

    @RequestMapping(value = "/groups", method = RequestMethod.POST)
    public ResponseEntity postGroups(@RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            log.info("Parsing Group File");
            try {
                businessGroupService.createGroupsFromCSV(file);
                return ResponseEntity.ok().build();
            } catch (EntityCreationException e) {
                log.info("Groups not created");
                log.info(e.getMessage());
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @RequestMapping(value = {"/groups", "s/homeoffice/cts/allTeams"}, method = RequestMethod.GET)
    public ResponseEntity<UnitRecord> getGroups(){
        log.info("All Groups requested");
        try {
            UnitRecord groups = businessGroupService.getAllGroups();
            return ResponseEntity.ok(groups);
        } catch (ListNotFoundException e) {
            log.info("\"All Groups\" not found");
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/groups", method = RequestMethod.PUT)
    public ResponseEntity<UserRecord> putGroups(@RequestParam("file") MultipartFile file) {
        throw new NotImplementedException();
    }

    //This is a create script, to be used once per new environment, maybe in the future this could just POST to alfresco directly.
    @RequestMapping(value = "/groups/export", method = RequestMethod.GET)
    public ResponseEntity<UnitCreateRecord> getLegacyUnitsByReference() {
        log.info("export groups requested");
        try {
            UnitCreateRecord units = businessGroupService.getGroupsCreateList();
            return ResponseEntity.ok(units);
        } catch (ListNotFoundException e) {
            log.info("export groups not found");
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}