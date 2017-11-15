package uk.gov.digital.ho.hocs.legacy.topics;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.digital.ho.hocs.legacy.AbstractFilePasrer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DCUFileParser extends AbstractFilePasrer<CSVTopicLine> {

    @Getter
    private final Set<CSVTopicLine> lines;

    public DCUFileParser(MultipartFile file) {
        this.lines = parseDCUFile(file);
    }

    private static Set<CSVTopicLine> parseDCUFile(MultipartFile file) {
        List<CSVTopicLine> result = new ArrayList<>();

        BufferedReader br;
        try {
            String line;
            InputStream is = file.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                String[] lineArray = splitLine(line);
                String parentTopic = lineArray[1].trim();
                String topicName = lineArray[0].trim();
                String topicTeam = lineArray[2].trim();
                String topicUnit = lineArray[4].trim();
                result.add(new CSVTopicLine(parentTopic, topicName, topicUnit, topicTeam));
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        // Remove the heading.
        result.remove(0);
        return result.stream().collect(Collectors.toSet());
    }
}