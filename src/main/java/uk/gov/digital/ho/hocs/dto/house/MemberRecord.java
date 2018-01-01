package uk.gov.digital.ho.hocs.dto.house;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.model.Member;

@AllArgsConstructor
public class MemberRecord {

    @Getter
    private String displayName;

    @Getter
    private String referenceName;

    public static MemberRecord create(Member member) {
        return new MemberRecord(member.getDisplayName(), member.getReferenceName());
    }
}