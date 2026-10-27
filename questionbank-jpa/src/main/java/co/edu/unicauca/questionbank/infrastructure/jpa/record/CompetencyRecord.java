package co.edu.unicauca.questionbank.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** JPA record of a competency (table {@code competencies}). */
@Entity
@Table(name = "competencies")
public class CompetencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false)
    private String name;

    /** Required by JPA. */
    protected CompetencyRecord() {
    }

    /**
     * @param code unique code
     * @param name name
     */
    public CompetencyRecord(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return unique code */
    public String getCode() {
        return code;
    }

    /** @return name */
    public String getName() {
        return name;
    }
}
