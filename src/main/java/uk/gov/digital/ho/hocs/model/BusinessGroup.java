package uk.gov.digital.ho.hocs.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.digital.ho.hocs.exception.GroupCreationException;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groups")
@Access(AccessType.FIELD)
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"id", "deleted", "parentGroup", "subGroups", "users"})
public class BusinessGroup {

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

    @Column(name = "parent_group_id", nullable = false)
    @Getter
    private String parentGroup;

    @ManyToMany(mappedBy = "groups", fetch = FetchType.LAZY)
    @Getter
    @Setter
    private Set<User> users = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name ="parent_group_id", referencedColumnName = "id")
    @Getter
    @Setter
    private Set<BusinessGroup> subGroups = new HashSet<>();

    @Column(name = "deleted", nullable = false)
    @Getter
    @Setter
    private Boolean deleted = false;

    public BusinessGroup(String displayName) throws GroupCreationException {
        this(displayName,displayName);
    }

    public BusinessGroup(String displayName, String referenceName) throws GroupCreationException {
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