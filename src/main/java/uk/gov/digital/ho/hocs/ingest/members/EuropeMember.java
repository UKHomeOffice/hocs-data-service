package uk.gov.digital.ho.hocs.ingest.members;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EuropeMember {

    @JacksonXmlProperty(localName = "fullName")
    public String name;

    @JacksonXmlProperty(localName = "country")
    public String country;

}