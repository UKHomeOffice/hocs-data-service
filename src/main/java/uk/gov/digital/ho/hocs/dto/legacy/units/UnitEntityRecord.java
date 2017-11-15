package uk.gov.digital.ho.hocs.dto.legacy.units;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.BusinessGroup;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class UnitEntityRecord {

    private String authorityName;

    private String displayName;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<UnitEntityRecord> teams;

    public static UnitEntityRecord create(BusinessGroup unit) {
        List<UnitEntityRecord> teams = unit.getSubGroups().stream().filter(m -> m.getParentGroup() == null).map(UnitEntityRecord::create).collect(Collectors.toList());
        return new UnitEntityRecord(unit.getReferenceName(), unit.getDisplayName(), teams);
    }
}
