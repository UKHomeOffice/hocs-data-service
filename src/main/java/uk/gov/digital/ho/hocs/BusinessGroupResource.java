package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.digital.ho.hocs.dto.legacy.units.BusinessGroupRecord;
import uk.gov.digital.ho.hocs.dto.legacy.units.UnitCreateRecord;
import uk.gov.digital.ho.hocs.dto.legacy.users.UserRecord;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.GroupCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.ingest.units.CSVBusinessGroupLine;
import uk.gov.digital.ho.hocs.ingest.units.UnitFileParser;
import uk.gov.digital.ho.hocs.model.BusinessGroup;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class BusinessGroupResource {
    private final BusinessGroupService businessGroupService;

    @Autowired
    public BusinessGroupResource(BusinessGroupService businessGroupService) {
        this.businessGroupService = businessGroupService;
    }

    @RequestMapping(value = "/groups", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<UserRecord> putGroups(@RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            log.info("Parsing Group File - PUT");
            try {
                Set<CSVBusinessGroupLine> lines = getCsvGroupLines(file);
                businessGroupService.updateBusinessGroups(lines);
                return ResponseEntity.ok().build();
            } catch (EntityCreationException | GroupCreationException e) {
                log.info("Groups not created");
                log.info(e.getMessage());
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @RequestMapping(value = {"/groups", "s/homeoffice/cts/allTeams"}, method = RequestMethod.GET)
    public ResponseEntity<List<BusinessGroupRecord>> getGroups(){
        log.info("All Groups requested");
        try {
            Set<BusinessGroup> groups = businessGroupService.getAllBusinessGroups();
            return ResponseEntity.ok(groups.stream().map(BusinessGroupRecord::create).collect(Collectors.toList()));
        } catch (ListNotFoundException e) {
            log.info("\"All Groups\" not found");
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "admin/groups/publish", method = RequestMethod.GET)
    public ResponseEntity<UnitCreateRecord> getLegacyUnitsByReference() {
        log.info("export groups requested");
        try {
            businessGroupService.getGroupsCreateList();
            return ResponseEntity.ok().build();
        } catch (ListNotFoundException e) {
            log.info("export groups not found");
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    private Set<CSVBusinessGroupLine> getCsvGroupLines(MultipartFile file) {
        Set<CSVBusinessGroupLine> lines;
        lines = new UnitFileParser(file).getLines();
        return lines;
    }
}