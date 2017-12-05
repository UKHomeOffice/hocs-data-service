package uk.gov.digital.ho.hocs.ingest.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@AllArgsConstructor
@Getter
@Slf4j
public class CSVUserLine {

    @Getter
    private String first;

    @Getter
    private String last;

    @Getter
    private String email;

    @Getter
    private List<String> groups;

}