package uk.gov.digital.ho.hocs.ingest.members;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WelshMembers {

    @JacksonXmlProperty(localName = "councillor")
    @JacksonXmlElementWrapper(localName = "councillor", useWrapping = false)
    private List<WelshMember> members;

}