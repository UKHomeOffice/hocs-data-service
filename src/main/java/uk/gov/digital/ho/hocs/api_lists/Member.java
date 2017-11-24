package uk.gov.digital.ho.hocs.api_lists;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Data
@NoArgsConstructor
class Member {

    @JacksonXmlProperty(localName = "DisplayAs")
    private String displayName;
    @JacksonXmlProperty(localName = "ListAs")
    private String listName;
    @JacksonXmlProperty(localName = "House")
    private String house;

}