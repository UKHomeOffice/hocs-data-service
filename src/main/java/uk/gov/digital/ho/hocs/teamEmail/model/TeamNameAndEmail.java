package uk.gov.digital.ho.hocs.teamEmail.model;

import lombok.*;

import javax.persistence.*;

@NoArgsConstructor
@Entity
@Table(name = "team_email")
@Access(AccessType.FIELD)
@EqualsAndHashCode(of = {"name"})
public class TeamNameAndEmail {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    @Getter
    @Setter
    private String name;

    @Column(name = "display_name", nullable = false)
    @Getter
    @Setter
    private String displayName;

    @Column(name = "email", nullable = false)
    @Getter
    @Setter
    private String email;

}
