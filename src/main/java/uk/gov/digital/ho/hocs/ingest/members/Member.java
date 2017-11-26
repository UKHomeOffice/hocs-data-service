package uk.gov.digital.ho.hocs.ingest.members;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Member {

    @JacksonXmlProperty(localName = "DisplayAs")
    public String displayName;
    @JacksonXmlProperty(localName = "ListAs")
    public String listName;
    @JacksonXmlProperty(localName = "House")
    public String house;

}