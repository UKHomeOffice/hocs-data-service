package uk.gov.digital.ho.hocs.ingest.members;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@JacksonXmlRootElement(localName = "councillorsbyward")
@Getter
@AllArgsConstructor
public class WelshWards {

    @JacksonXmlProperty(localName = "wards")
    @JacksonXmlElementWrapper(localName = "wards", useWrapping = true)
    private List<WelshWard> wards;

}