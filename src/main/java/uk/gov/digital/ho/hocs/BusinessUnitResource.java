package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.digital.ho.hocs.dto.units.BusinessUnitRecord;
import uk.gov.digital.ho.hocs.dto.units.PublishUnitRecord;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.GroupCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.ingest.units.CSVBusinessGroupLine;
import uk.gov.digital.ho.hocs.ingest.units.UnitFileParser;
import uk.gov.digital.ho.hocs.model.BusinessUnit;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class BusinessUnitResource {
    private final BusinessUnitService businessUnitService;

    @Autowired
    public BusinessUnitResource(BusinessUnitService businessUnitService) {
        this.businessUnitService = businessUnitService;
    }

    @RequestMapping(value = "/units", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity putGroups(@RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            log.info("Parsing Unit File");
            try {
                Set<CSVBusinessGroupLine> lines = getCsvGroupLines(file);
                businessUnitService.updateBusinessUnits(lines);
                return ResponseEntity.ok().build();
            } catch (EntityCreationException | GroupCreationException e) {
                log.info("Units not created");
                log.info(e.getMessage());
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @RequestMapping(value = {"/units", "s/homeoffice/cts/allTeams"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<BusinessUnitRecord>> getGroups(){
        log.info("All Units requested");
        Set<BusinessUnit> groups = businessUnitService.getAllBusinessUnits();
        return ResponseEntity.ok(groups.stream().map(BusinessUnitRecord::create).collect(Collectors.toList()));
    }

    @RequestMapping(value = "/units/publish", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<PublishUnitRecord> getLegacyUnitsByReference() {
        log.info("Export Units requested");
        try {
            return ResponseEntity.ok(businessUnitService.getGroupsCreateList());
        } catch (ListNotFoundException e) {
            log.info("No Units found");
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