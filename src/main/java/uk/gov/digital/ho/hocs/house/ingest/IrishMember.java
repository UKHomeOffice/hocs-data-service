package uk.gov.digital.ho.hocs.house.ingest;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class IrishMember {

    @JacksonXmlProperty(localName = "MemberFirstName")
    private String firstName;

    @JacksonXmlProperty(localName = "MemberLastName")
    private String lastName;

    public String getName()
    {
        return firstName + " " + lastName;
    }

}