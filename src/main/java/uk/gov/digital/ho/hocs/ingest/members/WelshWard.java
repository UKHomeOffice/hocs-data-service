package uk.gov.digital.ho.hocs.ingest.members;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WelshWard {

    @JacksonXmlProperty(localName = "councillors")
    @JacksonXmlElementWrapper(localName = "councillors", useWrapping = false)
    private WelshMembers members;

}