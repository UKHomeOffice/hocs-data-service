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
import uk.gov.digital.ho.hocs.model.TopicGroup;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class TopicsResource {
    private final TopicsService topicsService;

    @Autowired
    public TopicsResource(TopicsService topicsService) {
        this.topicsService = topicsService;
    }

    @RequestMapping(value = "/topics/{caseType}", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity updateTopicsList(@RequestParam("file") MultipartFile file, @PathVariable("caseType") String caseType) {
        if (!file.isEmpty()) {
            log.info("Parsing topics {}", caseType);
            try {
                Set<CSVTopicLine> lines = getCsvTopicLines(file, caseType);
                topicsService.updateTopics(lines, caseType);
                return ResponseEntity.ok().build();
            } catch (EntityCreationException e) {
                log.info("{} topics not created", caseType);
                log.info(e.getMessage());
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @RequestMapping(value = "/topics/{caseType}", method = RequestMethod.GET)
    public ResponseEntity<List<TopicGroupRecord>> getTopicListByReference(@PathVariable("caseType") String caseType) {
        log.info("List \"{}\" requested", caseType);
        try {
            Set<TopicGroup> topics = topicsService.getTopicByCaseType(caseType);
            return ResponseEntity.ok(topics.stream().map(TopicGroupRecord::create).collect(Collectors.toList()));
        } catch (ListNotFoundException e) {
            log.info("List \"{}\" not found", caseType);
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/topics", method = RequestMethod.GET)
    public ResponseEntity<List<TopicGroupRecord>> getLegacyListByReference() {
        log.info("List \"Legacy TopicList\" requested");
        try {
            Set<TopicGroup> topics = topicsService.getAllTopics();
            return ResponseEntity.ok(topics.stream().map(TopicGroupRecord::create).collect(Collectors.toList()));
        } catch (ListNotFoundException e) {
            log.info("List \"Legacy TopicList\" not found");
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    private Set<CSVTopicLine> getCsvTopicLines(MultipartFile file, String unitName) {
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