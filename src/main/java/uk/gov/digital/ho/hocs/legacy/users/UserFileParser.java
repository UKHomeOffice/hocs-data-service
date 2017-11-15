package uk.gov.digital.ho.hocs.legacy.users;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.digital.ho.hocs.legacy.AbstractFilePasrer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserFileParser extends AbstractFilePasrer<CSVUserLine> {

    @Getter
    private final Set<CSVUserLine> lines;

    public UserFileParser(MultipartFile file) {
        this.lines = parseUserTeamsFile(file);
    }

    private static Set<CSVUserLine> parseUserTeamsFile(MultipartFile file) {
        List<CSVUserLine> users = new ArrayList<>();

        // Build lines with user details then a truth table of which groups the user should be in
        BufferedReader br;
        try {
            String line;
            InputStream is = file.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                String[] lineArray = splitLine(line);
                String first = lineArray[0].trim();
                String last = lineArray[1].trim();
                String email = lineArray[2].trim();

                List<String> groups = new ArrayList<>();
                int i = 3;
                while (i < lineArray.length) {
                    groups.add(lineArray[i].trim());
                    i++;
                }

                users.add(new CSVUserLine(first, last, email, groups));
            }

            // Swap the ticks in the truth table for the real group names
            List<String> groupNames = users.get(0).getGroups();
            for (CSVUserLine user : users) {
                replaceGroupReferenceWithGroupName(user, groupNames);
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        // Remove the heading.
        users.remove(0);

        return users.stream().collect(Collectors.toSet());
    }

    private static void replaceGroupReferenceWithGroupName(CSVUserLine userLine, List<String> groupNames) {
        int i = 0;
        for (String group : userLine.getGroups()) {
            if (!group.isEmpty()) {
                userLine.getGroups().set(i, groupNames.get(i));
            }
            i++;
        }
        userLine.getGroups().removeAll(Arrays.asList(""));
    }

}