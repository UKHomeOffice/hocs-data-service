package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.dto.DataListRecord;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.model.DataList;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class DataListResource {
    private final DataListService dataListService;
    private final HouseService houseService;

    @Autowired
    public DataListResource(DataListService dataListService, HouseService houseService) {
        this.dataListService = dataListService;
        this.houseService = houseService;
    }

    @RequestMapping(value = "/list", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity postList(@RequestBody DataListRecord dlr) {
        log.info("Creating list \"{}\"", dlr.getName());
        try {
            dataListService.updateDataList(new DataList(dlr));
            return ResponseEntity.ok().build();
        } catch (EntityCreationException e) {
            log.info("List \"{}\" not created", dlr.getName());
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
        } catch (ListNotFoundException e){
            log.info("List \"{}\" not found", name);
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResponseEntity<List<DataListRecord>> getAllLists() {
        log.info("List \"Legacy TopicList\" requested");
        try {
            Set<DataList> lists = dataListService.getAllDataLists();
            return ResponseEntity.ok(lists.stream().map(DataListRecord::create).collect(Collectors.toList()));
        } catch (ListNotFoundException e) {
            log.info("List \"Legacy TopicList\" not found");
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

}