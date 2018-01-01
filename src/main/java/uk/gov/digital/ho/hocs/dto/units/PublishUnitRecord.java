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
public class PublishUnitRecord implements Serializable {

    @Getter
    private List<PublishUnitEntityRecord> manageGroups;

    public static PublishUnitRecord create(Set<BusinessGroup> list) {
        List<PublishUnitEntityRecord> groups = list.stream()
                .filter(m -> m.getParentGroup() == null)
                .map(PublishUnitEntityRecord::createGroups)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return new PublishUnitRecord(groups);
    }

}