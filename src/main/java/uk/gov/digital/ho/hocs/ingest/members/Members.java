package uk.gov.digital.ho.hocs.ingest.members;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@JacksonXmlRootElement(localName = "Members")
@Getter
@AllArgsConstructor
public class Members {

    @JacksonXmlProperty(localName = "Member")
    @JacksonXmlElementWrapper(localName = "Member", useWrapping = false)
    private List<UKMember> members;

}