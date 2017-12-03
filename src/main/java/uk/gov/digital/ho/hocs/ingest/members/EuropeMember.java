package uk.gov.digital.ho.hocs.ingest.members;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EuropeMember {

    @JacksonXmlProperty(localName = "fullName")
    private String name;

    @JacksonXmlProperty(localName = "country")
    private String country;

}