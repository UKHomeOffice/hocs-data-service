package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.digital.ho.hocs.dto.legacy.topics.TopicEntityRecord;
import uk.gov.digital.ho.hocs.dto.legacy.topics.TopicRecord;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.model.DataList;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Slf4j
public class LegacyResource {
    private final DataListResource dataListResource;
    private final LegacyService legacyService;

    @Autowired
    public LegacyResource(DataListResource dataListResource, LegacyService legacyService) {
        this.dataListResource = dataListResource;
        this.legacyService = legacyService;
    }

    @RequestMapping(value = "/legacy/topic/{name}", method = RequestMethod.POST)
    public ResponseEntity createTopicsListFromDCU(@RequestParam("file") MultipartFile file, @PathVariable("name") String name) {
        log.info("Parsing list \"TopicListDCU\"");
        if (!file.isEmpty()) {
            switch (name) {
                case "DCU":
                    DataList dataListDCU = legacyService.createDCUTopicsListFromCSV(file, "DCU_Topics", "DCU");
                    return dataListResource.createList(dataListDCU);
                case "UKVI":
                    DataList dataListUKVI = legacyService.createUKVITopicsListFromCSV(file, "UKVI_Topics", "UKVI");
                    return dataListResource.createList(dataListUKVI);
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @RequestMapping(value = {"/legacy/topic/TopicList", "/service/homeoffice/ctsv2/topicList"}, method = RequestMethod.GET)
    public ResponseEntity<List<TopicEntityRecord>> getLegacyListByReference() {
        log.info("List \"Legacy TopicList\" requested");
        try {
            TopicRecord dcu = legacyService.getLegacyTopicListByName("DCU_Topics");
            TopicRecord ukvi = legacyService.getLegacyTopicListByName("UKVI_Topics");

            List<TopicEntityRecord> retList = Stream.concat(dcu.getTopics().stream(), ukvi.getTopics().stream()).collect(Collectors.toList());
            return ResponseEntity.ok(retList);
        } catch (ListNotFoundException e) {
            log.info("List \"Legacy TopicList\" not found");
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

}
