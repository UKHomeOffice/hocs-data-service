package uk.gov.digital.ho.hocs.dto.units;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.BusinessGroup;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class UnitCreateRecord implements Serializable {
    private List<UnitCreateEntityRecord> manageGroups;

    public static UnitCreateRecord create(Set<BusinessGroup> list) {
        List<UnitCreateEntityRecord> groups = list.stream()
                .filter(m -> m.getParentGroup() == null)
                .map(UnitCreateEntityRecord::createGroups)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return new UnitCreateRecord(groups);
    }

}