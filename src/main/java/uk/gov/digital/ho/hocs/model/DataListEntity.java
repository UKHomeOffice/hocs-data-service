package uk.gov.digital.ho.hocs.model;

import lombok.*;
import uk.gov.digital.ho.hocs.dto.dataList.DataListEntityRecord;

import javax.persistence.*;

@NoArgsConstructor
@Entity
@Table(name = "entities")
@Access(AccessType.FIELD)
@EqualsAndHashCode(of = {"text", "value'"})
public class DataListEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "list_id")
    private Long listId;

    @Column(name = "text", nullable = false)
    @Getter
    private String text;

    @Column(name = "value", nullable = false)
    @Getter
    private String value;

    @Column(name = "deleted", nullable = false)
    @Getter
    @Setter
    private Boolean deleted = false;

    public DataListEntity(DataListEntityRecord dler){
        this.text = toListText(dler.getText());
        this.value = toListValue(dler.getValue());
    }

    private static String toListText(String text) {
        text = text.startsWith("\"") ? text.substring(1) : text;
        text = text.endsWith("\"") ? text.substring(0, text.length() - 1) : text;
        return text;
    }

    private static String toListValue(String value) {
        return value.replaceAll(" ", "_")
                .replaceAll("[^a-zA-Z0-9_]+", "")
                .replaceAll("__", "_")
                .toUpperCase();
    }
}