package uk.gov.digital.ho.hocs.house.ingest;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
class UKMember {

    @JacksonXmlProperty(localName = "DisplayAs")
    private String name;

}