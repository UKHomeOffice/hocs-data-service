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


public class TeamNameAndEmailParser extends AbstractFilePasrer<CSVTeamNameAndEmail> {

    @Getter
    private final Set<CSVTeamNameAndEmail> lines;

    public TeamNameAndEmailParser(MultipartFile file) {
        this.lines = ParseTeamEmailFile(file);
    }

    public static Set<CSVTeamNameAndEmail> ParseTeamEmailFile(MultipartFile file) {
        List<CSVTeamNameAndEmail> result = new ArrayList<>();
        BufferedReader br;
        try {
            String line;
            InputStream is = file.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                String[] lineArray = splitLine(line);
                String displayName = lineArray[0].trim();
                String name = lineArray[1].trim();
                String email = lineArray[2].trim();
                result.add(new CSVTeamNameAndEmail(displayName, name, email));
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        // Remove the heading.
        result.remove(0);
        return new HashSet<>(result);
    }
}
