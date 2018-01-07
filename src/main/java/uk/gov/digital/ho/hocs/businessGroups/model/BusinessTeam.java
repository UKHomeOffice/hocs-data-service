package uk.gov.digital.ho.hocs.businessGroups.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.user.model.User;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Entity
@Table(name = "teams")
@Access(AccessType.FIELD)
@EqualsAndHashCode(of = {"displayName", "referenceName"})
public class BusinessTeam implements Serializable {

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

    @Column(name = "unit_id", nullable = false)
    @Getter
    private String unit;

    @ManyToMany(mappedBy = "teams", fetch = FetchType.LAZY)
    @Getter
    @Setter
    private Set<User> users = new HashSet<>();

    @Column(name = "deleted", nullable = false)
    @Getter
    @Setter
    private Boolean deleted = false;

    public BusinessTeam(String displayName) throws EntityCreationException {
        this(displayName,displayName);
    }

    public BusinessTeam(String displayName, String referenceName) throws EntityCreationException {
        this.displayName = toDisplayName(displayName);
        this.referenceName = toReferenceName(referenceName);
    }

    private static String toDisplayName(String text) {
        text = text.startsWith("\"") ? text.substring(1) : text;
        text = text.endsWith("\"") ? text.substring(0, text.length() - 1) : text;
        return text;
    }

    private static String toReferenceName(String value) throws EntityCreationException {
        if (value.length() > 94) {
            throw new EntityCreationException("Group name exceeds size limit");
        }
        return "GROUP_" + value.replaceAll(" ", "_")
                .replaceAll("[^a-zA-Z0-9_]+", "")
                .replaceAll("__", "_")
                .toUpperCase();
    }
}