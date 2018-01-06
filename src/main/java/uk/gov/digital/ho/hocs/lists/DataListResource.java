package uk.gov.digital.ho.hocs.lists;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.lists.dto.DataListRecord;
import uk.gov.digital.ho.hocs.lists.model.DataList;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class DataListResource {
    private final DataListService dataListService;

    @Autowired
    public DataListResource(DataListService dataListService) {
        this.dataListService = dataListService;
    }

    @RequestMapping(value = "/list", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity postList(@RequestBody DataListRecord dlr) {
        log.info("Creating list \"{}\"", dlr.getName());
        try {
            dataListService.updateDataList(new DataList(dlr));
            return ResponseEntity.ok().build();
        } catch(EntityCreationException e) {
            log.info("Unable to update List \"{}\" ", dlr.getName());
            log.info(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/list/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DataListRecord> getListByName(@PathVariable("name") String name) {
        log.info("List \"{}\" requested", name);
        try {
            DataList list = dataListService.getDataListByName(name);
            return ResponseEntity.ok(DataListRecord.create(list));
        } catch (EntityNotFoundException e) {
            log.info("List \"{}\" not found", name);
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<DataListRecord>> getAllLists() {
        log.info("All Lists requested");
        Set<DataList> lists = dataListService.getAllDataLists();
        return ResponseEntity.ok(lists.stream().map(DataListRecord::create).collect(Collectors.toList()));
    }

}