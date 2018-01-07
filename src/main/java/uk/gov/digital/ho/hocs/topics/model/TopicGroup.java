package uk.gov.digital.ho.hocs.topics.model;

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
@Table(name = "topic_groups")
@Access(AccessType.FIELD)
@EqualsAndHashCode(of = {"name", "caseType"})
public class TopicGroup implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    @Getter
    private String name;

    @Column(name = "case_type", nullable = false)
    @Getter
    private String caseType;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name ="parent_topic_id", referencedColumnName = "id")
    @Getter
    @Setter
    private Set<Topic> topicListItems = new HashSet<>();

    @Column(name = "deleted", nullable = false)
    @Getter
    @Setter
    private Boolean deleted = false;

    public TopicGroup(String name, String caseType) {
        this.name = toDisplayName(name);
        this.caseType = caseType;
    }

    private static String toDisplayName(String text) {
        text = text.startsWith("\"") ? text.substring(1) : text;
        text = text.endsWith("\"") ? text.substring(0, text.length() - 1) : text;
        return text;
    }
}