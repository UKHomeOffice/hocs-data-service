package uk.gov.digital.ho.hocs.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Entity
@Table(name = "users")
@Access(AccessType.FIELD)
@EqualsAndHashCode(of = "userName")
public class User implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    @Getter
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @Getter
    private String lastName;

    @Column(name = "user_name", nullable = false)
    @Getter
    private String userName;

    @Column(name = "email", nullable = false)
    @Getter
    private String emailAddress;

    @Column(name = "department", nullable = false)
    @Getter
    private String department;

    @Column(name = "deleted", nullable = false)
    @Getter
    @Setter
    private Boolean deleted = false;

    @ManyToMany()
    @JoinTable(
            name = "users_teams",
            joinColumns = { @JoinColumn(name = "user_id") },
            inverseJoinColumns = { @JoinColumn(name = "team_id") }
    )
    @Getter
    @Setter
    Set<BusinessTeam> teams = new HashSet<>();

    public User(String firstName, String lastName, String userName, String emailAddress, String department) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.emailAddress = emailAddress;
        this.department = department;
    }
}