package uk.gov.digital.ho.hocs.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "properties")
@Access(AccessType.FIELD)
@NoArgsConstructor
@EqualsAndHashCode(of = {"key", "value"})
public class DataListEntityProperty {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_id")
    private Long entityid;

    @Column(name = "key")
    @Getter
    private String key;

    @Column(name = "value")
    @Getter
    private String value;

    public DataListEntityProperty(String key, String value)
    {
        this.key = key;
        this.value = value;
    }
}