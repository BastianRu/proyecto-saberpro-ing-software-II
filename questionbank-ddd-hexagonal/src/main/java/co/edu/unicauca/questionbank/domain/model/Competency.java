package co.edu.unicauca.questionbank.domain.model;

/** Saber Pro competency a question helps to assess (for example, "Problem analysis"). */
public class Competency {

    private Long id;
    private final String code;
    private final String name;

    /**
     * Creates a competency.
     *
     * @param id   identity, or null for a new competency
     * @param code unique short code, such as "SOL-DES"
     * @param name descriptive name
     */
    public Competency(Long id, String code, String name) {
        this.id = id;
        this.code = Require.text(code, "competency code");
        this.name = Require.text(name, "competency name");
    }

    /** @return identity, or null if not persisted yet */
    public Long getId() {
        return id;
    }

    /** @param id identity assigned by the repository */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return unique short code */
    public String getCode() {
        return code;
    }

    /** @return descriptive name */
    public String getName() {
        return name;
    }
}
