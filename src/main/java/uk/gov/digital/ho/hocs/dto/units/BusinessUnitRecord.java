package uk.gov.digital.ho.hocs.dto.units;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.BusinessUnit;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class BusinessUnitRecord implements Serializable {

    @Getter
    private String authorityName;

    @Getter
    private String displayName;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    private List<BusinessTeamRecord> teams;

    public static BusinessUnitRecord create(BusinessUnit unit) {
        return create(unit,false);
    }

    public static BusinessUnitRecord create(BusinessUnit unit, boolean showDeleted) {
        List<BusinessTeamRecord> teams = unit.getTeams().stream().filter(m -> !m.getDeleted() || showDeleted).map(BusinessTeamRecord::create).collect(Collectors.toList());
        return new BusinessUnitRecord(unit.getReferenceName(), unit.getDisplayName(), teams);
    }
}
