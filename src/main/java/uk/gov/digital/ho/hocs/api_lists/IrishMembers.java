package uk.gov.digital.ho.hocs.api_lists;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JacksonXmlRootElement(localName = "ArrayOfMember")
@Data
@NoArgsConstructor
class IrishMembers {

    @JacksonXmlProperty(localName = "Member")
    @JacksonXmlElementWrapper(localName = "Member", useWrapping = false)
    private List<IrishMember> members;

}