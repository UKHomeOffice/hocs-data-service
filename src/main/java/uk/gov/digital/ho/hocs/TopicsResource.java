package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.digital.ho.hocs.dto.legacy.topics.TopicGroupRecord;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.ingest.topics.CSVTopicLine;
import uk.gov.digital.ho.hocs.ingest.topics.DCUFileParser;
import uk.gov.digital.ho.hocs.ingest.topics.UKVIFileParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Slf4j
public class TopicsResource {
    private final TopicsService topicsService;

    @Autowired
    public TopicsResource(TopicsService topicsService) {
        this.topicsService = topicsService;
    }


    @RequestMapping(value = "/topics/{unitName}", method = RequestMethod.POST)
    public ResponseEntity createTopicsListFromDCU(@RequestParam("file") MultipartFile file, @PathVariable("unitName") String unitName) {
        if (!file.isEmpty()) {
            log.info("Parsing topics {} - POST", unitName);
            try {
                Set<CSVTopicLine> lines = getCsvTopicLines(file, unitName);
                topicsService.createTopics(lines, unitName);
                return ResponseEntity.ok().build();
            } catch (EntityCreationException e) {
                log.info("{} topics not created", unitName);
                log.info(e.getMessage());
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @RequestMapping(value = "topics/{unitName}", method = RequestMethod.PUT)
    public ResponseEntity updateTopicsListFromDCU(@RequestParam("file") MultipartFile file, @PathVariable("unitName") String unitName) {
        if (!file.isEmpty()) {
            log.info("Parsing topics {} - PUT", unitName);
            try {
                Set<CSVTopicLine> lines = getCsvTopicLines(file, unitName);
                topicsService.updateTopics(lines, unitName);
                return ResponseEntity.ok().build();
            } catch (EntityCreationException e) {
                log.info("{} topics not created", unitName);
                log.info(e.getMessage());
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @RequestMapping(value = {"/topics/topicList", "/service/homeoffice/ctsv2/topicList"}, method = RequestMethod.GET)
    public ResponseEntity<List<TopicGroupRecord>> getLegacyListByReference() {
        log.info("List \"Legacy TopicList\" requested");
        try {

            List<TopicGroupRecord> topics = new ArrayList<>();
            String[] caseTypes = {"DCU", "UKVI", "FOI"};

            for (String caseType : caseTypes) {
                List<TopicGroupRecord> topicList = topicsService.getTopicByCaseType(caseType);
                topics = Stream
                        .concat(topics.stream(), topicList.stream())
                        .collect(Collectors.toList());
            }

            return ResponseEntity.ok(topics);

        } catch (ListNotFoundException e) {
            log.info("List \"Legacy TopicList\" not found");
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    private Set<CSVTopicLine> getCsvTopicLines(@RequestParam("file") MultipartFile file, @PathVariable("unitName") String unitName) {
        Set<CSVTopicLine> lines;
        switch (unitName) {
            case "DCU":
            case "FOI":
                lines = new DCUFileParser(file).getLines();
                break;
            case "UKVI":
                lines = new UKVIFileParser(file).getLines();
                break;
            default:
                throw new EntityCreationException("Unknown Business Unit");
        }
        return lines;
    }

}