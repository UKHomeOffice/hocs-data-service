package uk.gov.digital.ho.hocs.businessGroups.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.businessGroups.model.BusinessTeam;
import uk.gov.digital.ho.hocs.businessGroups.model.BusinessUnit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class PublishUnitEntityRecord implements Serializable {

    @Getter
    private String action;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Getter
    private String unitDisplayName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Getter
    private String unitRefName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Getter
    private String teamDisplayName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Getter
    private String teamRefName;

    static PublishUnitEntityRecord createUnit(BusinessUnit unit) {

       String action = "addUnit";
       String unitDisplayName = unit.getDisplayName();
       String unitRefName = unit.getReferenceName();

        return new PublishUnitEntityRecord(action, unitDisplayName, unitRefName, null, null);
    }

    static PublishUnitEntityRecord createTeam(BusinessTeam team, String unitReferenceName) {

        String action = "addTeam";
        String teamDisplayName = team.getDisplayName();
        String teamRefName = team.getReferenceName();

        return new PublishUnitEntityRecord(action, null, unitReferenceName, teamDisplayName, teamRefName);
    }

    // Units and Teams are added at the same level, all in one manageGroups object.
    static List<PublishUnitEntityRecord> createGroups(BusinessUnit unit) {
        List<PublishUnitEntityRecord> list = new ArrayList<>();
        list.add(PublishUnitEntityRecord.createUnit(unit));
        for (BusinessTeam team : unit.getTeams()) {
            list.add(PublishUnitEntityRecord.createTeam(team, unit.getReferenceName()));
        }
        return list;
    }
}