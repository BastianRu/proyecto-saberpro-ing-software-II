package co.edu.unicauca.questionbank.domain.model;

/** Academic topic that groups related questions (for example, "Software architecture"). */
public class Topic {

    private Long id;
    private final String name;

    /**
     * Creates a topic.
     *
     * @param id   identity, or null for a new topic
     * @param name unique topic name
     */
    public Topic(Long id, String name) {
        this.id = id;
        this.name = Require.text(name, "topic name");
    }

    /** @return identity, or null if not persisted yet */
    public Long getId() {
        return id;
    }

    /** @param id identity assigned by the repository */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return topic name */
    public String getName() {
        return name;
    }
}
