package co.edu.unicauca.questionbank.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** JPA record of a topic (table {@code topics}). */
@Entity
@Table(name = "topics")
public class TopicRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    /** Required by JPA. */
    protected TopicRecord() {
    }

    /** @param name topic name */
    public TopicRecord(String name) {
        this.name = name;
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return topic name */
    public String getName() {
        return name;
    }
}
