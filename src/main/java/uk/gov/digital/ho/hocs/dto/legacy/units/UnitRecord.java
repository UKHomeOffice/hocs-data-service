package uk.gov.digital.ho.hocs.dto.legacy.units;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.BusinessGroup;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class UnitRecord implements Serializable {
    private List<UnitEntityRecord> units;

    public static UnitRecord create(List<BusinessGroup> list) {
        List<UnitEntityRecord> units = list.stream().map(UnitEntityRecord::create).collect(Collectors.toList());
        return new UnitRecord(units);
    }

}