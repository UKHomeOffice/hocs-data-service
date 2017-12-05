package uk.gov.digital.ho.hocs.ingest.members;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Member {

    @JacksonXmlProperty(localName = "DisplayAs")
    private String displayName;
    @JacksonXmlProperty(localName = "ListAs")
    private String listName;
    @JacksonXmlProperty(localName = "House")
    private String house;

}