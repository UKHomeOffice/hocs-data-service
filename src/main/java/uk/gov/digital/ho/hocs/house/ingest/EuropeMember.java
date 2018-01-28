package uk.gov.digital.ho.hocs.house.ingest;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EuropeMember {

    @JacksonXmlProperty(localName = "fullName")
    private String name;

    public String getName()
    {
        return name;
    }

}