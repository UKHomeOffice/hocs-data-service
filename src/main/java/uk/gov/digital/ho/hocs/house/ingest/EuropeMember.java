package uk.gov.digital.ho.hocs.house.ingest;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import net.logstash.logback.encoder.org.apache.commons.lang.WordUtils;

@AllArgsConstructor
public class EuropeMember {

    @JacksonXmlProperty(localName = "fullName")
    private String name;

    public String getName()
    {
        return WordUtils.capitalizeFully(name.toLowerCase(), new char[]{' ', '\'', '-', '('});
    }

}