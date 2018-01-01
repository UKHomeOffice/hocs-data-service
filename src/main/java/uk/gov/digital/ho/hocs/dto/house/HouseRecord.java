package uk.gov.digital.ho.hocs.dto.house;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.House;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class HouseRecord {

    @Getter
    private String name;

    @Getter
    private List<MemberRecord> members = new ArrayList<>();

    public static HouseRecord create(House house) {
        return create(house, false);
    }

    public static HouseRecord create(House house, boolean showDeleted) {
        List<MemberRecord> memberList = house.getMembers().stream().filter(member -> !member.getDeleted() || showDeleted).map(MemberRecord::create).collect(Collectors.toList());
        return new HouseRecord(house.getName(), memberList);
    }
}