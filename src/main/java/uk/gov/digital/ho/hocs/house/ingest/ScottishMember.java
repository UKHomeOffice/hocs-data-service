package uk.gov.digital.ho.hocs.house.ingest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import net.logstash.logback.encoder.org.apache.commons.lang.ArrayUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class ScottishMember {

    @JsonProperty(value = "ParliamentaryName")
    private String name;

    public String getName() {
        String[] names = name.split(",", 2);
        ArrayUtils.reverse(names);
        return String.join(" ", names).trim();
    }

}
