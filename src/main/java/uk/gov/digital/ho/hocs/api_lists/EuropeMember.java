package uk.gov.digital.ho.hocs.api_lists;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Data
@NoArgsConstructor
class EuropeMember {

    @JacksonXmlProperty(localName = "fullName")
    private String name;

    @JacksonXmlProperty(localName = "country")
    private String country;

}