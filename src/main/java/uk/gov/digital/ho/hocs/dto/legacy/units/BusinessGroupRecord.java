package uk.gov.digital.ho.hocs.dto.legacy.units;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.BusinessGroup;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class BusinessGroupRecord implements Serializable {

    private String authorityName;

    private String displayName;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<BusinessGroupRecord> teams;

    public static BusinessGroupRecord create(BusinessGroup unit) {
        List<BusinessGroupRecord> teams = unit.getSubGroups().stream().filter(m -> m.getParentGroup() == null).map(BusinessGroupRecord::create).collect(Collectors.toList());
        return new BusinessGroupRecord(unit.getReferenceName(), unit.getDisplayName(), teams);
    }
}
