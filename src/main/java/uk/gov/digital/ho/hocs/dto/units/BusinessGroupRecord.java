package uk.gov.digital.ho.hocs.dto.units;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.BusinessGroup;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class BusinessGroupRecord implements Serializable {

    @Getter
    private String authorityName;

    @Getter
    private String displayName;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    private List<BusinessGroupRecord> teams;

    public static BusinessGroupRecord create(BusinessGroup unit) {
        return create(unit,false);
    }

    public static BusinessGroupRecord create(BusinessGroup unit, boolean showDeleted) {
        List<BusinessGroupRecord> teams = unit.getSubGroups().stream().filter(m -> m.getParentGroup() == null).filter(m -> !m.getDeleted() || showDeleted).map(BusinessGroupRecord::create).collect(Collectors.toList());
        return new BusinessGroupRecord(unit.getReferenceName(), unit.getDisplayName(), teams);
    }
}
