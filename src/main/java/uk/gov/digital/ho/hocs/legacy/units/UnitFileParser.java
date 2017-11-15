package uk.gov.digital.ho.hocs.legacy.units;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.digital.ho.hocs.legacy.AbstractFilePasrer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class UnitFileParser extends AbstractFilePasrer<CSVGroupLine> {

    @Getter
    private final Set<CSVGroupLine> lines;

    public UnitFileParser(MultipartFile file) {
        this.lines = parseUnitTeamsFile(file);
    }

    private static Set<CSVGroupLine> parseUnitTeamsFile(MultipartFile file) {
        Set<CSVGroupLine> groups = new HashSet<>();

        BufferedReader br;
        try {
            String line;
            InputStream is = file.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                String[] lineArray = splitLine(line);
                String unit = lineArray[1].trim();
                String team = lineArray[2].trim();
                groups.add(new CSVGroupLine(unit, team));
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        // Remove the heading.
        groups.remove(0);

        return groups;
    }

}