package uk.gov.digital.ho.hocs.dto.legacy.units;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.BusinessGroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public class UnitCreateEntityRecord implements Serializable {

    private String action;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String unitDisplayName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String unitRefName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String teamDisplayName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String teamRefName;

    public static UnitCreateEntityRecord createUnit(BusinessGroup unit) {

       String action = "addUnit";
       String unitDisplayName = unit.getDisplayName();
       String unitRefName = unit.getReferenceName();
       String teamDisplayName = null;
       String teamRefName = null;

        return new UnitCreateEntityRecord(action, unitDisplayName, unitRefName, teamDisplayName, teamRefName);
    }

    public static UnitCreateEntityRecord createTeam(BusinessGroup team, String unitReferenceName) {

        String action = "addTeam";
        String unitDisplayName = null;
        String unitRefName = unitReferenceName;
        String teamDisplayName = team.getDisplayName();
        String teamRefName = team.getReferenceName();

        return new UnitCreateEntityRecord(action, unitDisplayName, unitRefName, teamDisplayName, teamRefName);
    }

    // Units and Teams are added at the same level, all in one manageGroups object.
    public static List<UnitCreateEntityRecord> createGroups(BusinessGroup unit) {
        List<UnitCreateEntityRecord> list = new ArrayList<>();
        list.add(UnitCreateEntityRecord.createUnit(unit));
        for (BusinessGroup team : unit.getSubGroups()) {
            list.add(UnitCreateEntityRecord.createTeam(team, unit.getReferenceName()));
        }
        return list;
    }
}