package uk.gov.digital.ho.hocs.dto.legacy.units;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.BusinessGroup;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class UnitCreateRecord {
    private List<UnitCreateEntityRecord> manageGroups;

    public static UnitCreateRecord create(List<BusinessGroup> list) {
        List<UnitCreateEntityRecord> groups = list.stream()
                .filter(m -> m.getParentGroup() == null)
                .map(UnitCreateEntityRecord::createGroups)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return new UnitCreateRecord(groups);
    }

}