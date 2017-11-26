package uk.gov.digital.ho.hocs.ingest.members;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@AllArgsConstructor
public class ScottishMember {

    @JsonProperty(value = "ParliamentaryName")
    public String name;

}
