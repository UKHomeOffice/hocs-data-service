package uk.gov.digital.ho.hocs.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@Table(name = "topic")
@Access(AccessType.FIELD)
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"id", "deleted", "parentTopic"})
public class Topic {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    @Getter
    private String name;

    @Column(name = "owning_unit", nullable = false)
    @Getter
    private String topicUnit;

    @Column(name = "owning_team", nullable = false)
    @Getter
    private String topicTeam;

    @Column(name = "parent_topic_id", nullable = false)
    @Getter
    private String parentTopic;

    @Column(name = "deleted", nullable = false)
    @Getter
    @Setter
    private Boolean deleted;

    public Topic(String name, String owningUnit, String owningTeam) {
        this.name = toDisplayName(name);
        this.topicTeam = owningTeam;
        this.topicUnit = owningUnit;
        this.deleted = false;
    }

    private static String toDisplayName(String text) {
        text = text.startsWith("\"") ? text.substring(1) : text;
        text = text.endsWith("\"") ? text.substring(0, text.length() - 1) : text;
        return text;
    }

}