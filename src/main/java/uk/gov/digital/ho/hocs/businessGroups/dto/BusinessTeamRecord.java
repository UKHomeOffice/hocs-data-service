package uk.gov.digital.ho.hocs.businessGroups.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.businessGroups.model.BusinessTeam;

import java.io.Serializable;

@AllArgsConstructor
public class BusinessTeamRecord implements Serializable {

    @Getter
    private String authorityName;

    @Getter
    private String displayName;

    public static BusinessTeamRecord create(BusinessTeam team) {
        return create(team,false);
    }

    public static BusinessTeamRecord create(BusinessTeam team, boolean showDeleted) {
        return new BusinessTeamRecord(team.getReferenceName(), team.getDisplayName());
    }
}
