package uk.gov.digital.ho.hocs.teamEmail.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamNameAndEmailDto {

    @Getter
    private String email;

    @Getter
    private String displayName;

}
