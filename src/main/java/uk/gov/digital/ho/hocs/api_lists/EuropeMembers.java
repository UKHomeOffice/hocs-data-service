package uk.gov.digital.ho.hocs.api_lists;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JacksonXmlRootElement(localName = "meps")
@Data
@NoArgsConstructor
class EuropeMembers {

    @JacksonXmlProperty(localName = "mep")
    @JacksonXmlElementWrapper(localName = "mep", useWrapping = false)
    private List<EuropeMember> members;

}