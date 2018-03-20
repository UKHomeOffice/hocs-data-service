package uk.gov.digital.ho.hocs.teamEmail.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@Entity
@Table(name = "team_email")
@Access(AccessType.FIELD)
@EqualsAndHashCode(of = {"name"})
public class TeamEmail {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    @Getter
    @Setter
    private String name;

    @Column(name = "email", nullable = false)
    @Getter
    @Setter
    private String email;

}
