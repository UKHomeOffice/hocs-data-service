package uk.gov.digital.ho.hocs.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "topic_groups")
@Access(AccessType.FIELD)
@NoArgsConstructor
@EqualsAndHashCode()
public class TopicGroup {

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