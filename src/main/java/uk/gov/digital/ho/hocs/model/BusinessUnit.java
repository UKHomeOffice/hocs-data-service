package uk.gov.digital.ho.hocs.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.digital.ho.hocs.exception.GroupCreationException;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Entity
@Table(name = "units")
@Access(AccessType.FIELD)
@EqualsAndHashCode(of = {"displayName", "referenceName"})
public class BusinessUnit implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_name", nullable = false)
    @Getter
    private String displayName;

    @Column(name = "reference_name", nullable = false)
    @Getter
    private String referenceName;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name ="unit_id", referencedColumnName = "id")
    @Getter
    @Setter
    private Set<BusinessTeam> teams = new HashSet<>();

    @Column(name = "deleted", nullable = false)
    @Getter
    @Setter
    private Boolean deleted = false;

    public BusinessUnit(String displayName) throws GroupCreationException {
        this(displayName,displayName);
    }

    public BusinessUnit(String displayName, String referenceName) throws GroupCreationException {
        this.displayName = toDisplayName(displayName);
        this.referenceName = toReferenceName(referenceName);
    }

    private static String toDisplayName(String text) {
        text = text.startsWith("\"") ? text.substring(1) : text;
        text = text.endsWith("\"") ? text.substring(0, text.length() - 1) : text;
        return text;
    }

    private static String toReferenceName(String value) throws GroupCreationException {
        if (value.length() > 94) {
            throw new GroupCreationException("Group name exceeds size limit");
        }
        return "GROUP_" + value.replaceAll(" ", "_")
                .replaceAll("[^a-zA-Z0-9_]+", "")
                .replaceAll("__", "_")
                .toUpperCase();
    }
}