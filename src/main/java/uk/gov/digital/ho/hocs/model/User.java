package uk.gov.digital.ho.hocs.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Access(AccessType.FIELD)
@NoArgsConstructor
@EqualsAndHashCode(of = "userName")
public class User {

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

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "users_groups",
            joinColumns = { @JoinColumn(name = "user_id") },
            inverseJoinColumns = { @JoinColumn(name = "group_id") }
    )
    @Getter
    @Setter
    Set<BusinessGroup> groups = new HashSet<>();

    public User(String firstName, String lastName, String userName, String emailAddress, String department) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.emailAddress = emailAddress;
        this.department = department;
    }
}