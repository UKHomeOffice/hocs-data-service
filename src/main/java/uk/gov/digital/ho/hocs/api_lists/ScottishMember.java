package uk.gov.digital.ho.hocs.api_lists;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class ScottishMember {

    @JsonProperty(value = "ParliamentaryName")
    String name;

}
