package uk.gov.digital.ho.hocs.ingest.units;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.digital.ho.hocs.ingest.AbstractFilePasrer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class UnitFileParser extends AbstractFilePasrer<CSVBusinessGroupLine> {

    @Getter
    private final Set<CSVBusinessGroupLine> lines;

    public UnitFileParser(MultipartFile file) {
        this.lines = parseUnitTeamsFile(file);
    }

    private static Set<CSVBusinessGroupLine> parseUnitTeamsFile(MultipartFile file) {
        Set<CSVBusinessGroupLine> groups = new HashSet<>();

        BufferedReader br;
        try {
            String line;
            InputStream is = file.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                String[] lineArray = splitLine(line);
                String unitDisplay = lineArray[1].trim();
                String unitReference = lineArray[2].trim();
                String teamDisplay = lineArray[3].trim();
                String teamReference = lineArray[4].trim();
                groups.add(new CSVBusinessGroupLine(unitDisplay, unitReference, teamDisplay, teamReference));
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        // Remove the heading.
        groups.remove(0);

        return groups;
    }

}