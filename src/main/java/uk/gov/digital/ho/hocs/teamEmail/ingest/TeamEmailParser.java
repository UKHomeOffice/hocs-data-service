package uk.gov.digital.ho.hocs.teamEmail.ingest;

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


public class TeamEmailParser extends AbstractFilePasrer<CSVTeamEmail> {

    @Getter
    private final Set<CSVTeamEmail> lines;

    public TeamEmailParser(MultipartFile file) {
        this.lines = ParseTeamEmailFile(file);
    }

    public static Set<CSVTeamEmail> ParseTeamEmailFile(MultipartFile file) {
        List<CSVTeamEmail> result = new ArrayList<>();
        BufferedReader br;
        try {
            String line;
            InputStream is = file.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                String[] lineArray = splitLine(line);
                String name = lineArray[0].trim();
                String email = lineArray[1].trim();
                result.add(new CSVTeamEmail(name, email));
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        // Remove the heading.
        result.remove(0);
        return new HashSet<>(result);
    }
}
