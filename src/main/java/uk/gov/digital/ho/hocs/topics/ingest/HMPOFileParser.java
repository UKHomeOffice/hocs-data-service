package uk.gov.digital.ho.hocs.topics.ingest;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.digital.ho.hocs.lists.ingest.AbstractFilePasrer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HMPOFileParser extends AbstractFilePasrer<CSVTopicLine> {

    @Getter
    private final Set<CSVTopicLine> lines;

    public HMPOFileParser(MultipartFile file) {
        this.lines = parseHMPOFile(file);
    }

    private static Set<CSVTopicLine> parseHMPOFile(MultipartFile file) {
        List<CSVTopicLine> result = new ArrayList<>();

        BufferedReader br;
        try {
            String line;
            InputStream is = file.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                String[] lineArray = splitLine(line);
                String parentTopic = lineArray[0].trim();
                String topicName = lineArray[1].trim();
                result.add(new CSVTopicLine(parentTopic, topicName, "GROUP_HMPO_CORRESPONDENCE_AND_COMPLAINTS", null));
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        // Remove the heading.
        result.remove(0);
        return new HashSet<>(result);
    }
}