package uk.gov.digital.ho.hocs.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.BusinessGroup;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class BusinessGroupRecord {

    private String displayName;

    private String referenceName;

    private List<BusinessGroupRecord> subGroups;

    public static BusinessGroupRecord create(BusinessGroup group) {
        List<BusinessGroupRecord> subGroupRecords = group.getSubGroups()
                .stream()
                .map(BusinessGroupRecord::create)
                .collect(Collectors.toList());
        return new BusinessGroupRecord(group.getDisplayName(), group.getReferenceName(), subGroupRecords);
    }

}
