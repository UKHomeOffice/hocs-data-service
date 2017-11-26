package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.dto.DataListRecord;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.ingest.members.ListConsumerService;

@RestController
@Slf4j
public class MemberResource {
    private final DataListService dataListService;
    private final ListConsumerService listConsumerService;

    @Autowired
    public MemberResource(DataListService dataListService, ListConsumerService listConsumerService) {
        this.dataListService = dataListService;
        this.listConsumerService = listConsumerService;
    }

    @RequestMapping(value = "/members/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DataListRecord> getMinisterListByName(@PathVariable("name") String name) {
        log.info("List \"{}\" requested", name);
        try {

            DataListRecord list;
            switch(name) {
                case "ukvi_member_list":
                    list = dataListService.getCombinedList(name,
                            "commons_list",
                            "lords_list",
                            "scottish_parliament_list",
                            "northern_irish_assembly_list",
                            "european_parliament_list",
                            "welsh_assembly_list");
                    break;
                default:
                    list = dataListService.getListByName(name);
            }
            return ResponseEntity.ok(list);

        } catch (ListNotFoundException e){
            log.info("List \"{}\" not found", name);
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
