package co.edu.unicauca.questionbank.domain.model;

/** A person who interacts with the question bank under a given role. */
public class User {

    private Long id;
    private final String username;
    private final String fullName;
    private final String email;
    private final Role role;

    /**
     * Creates a user.
     *
     * @param id       identity assigned by the repository, or null for a new user
     * @param username unique login name
     * @param fullName full name
     * @param email    unique email address
     * @param role     role played in the system
     */
    public User(Long id, String username, String fullName, String email, Role role) {
        this.id = id;
        this.username = Require.text(username, "username");
        this.fullName = Require.text(fullName, "full name");
        this.email = Require.text(email, "email");
        this.role = Require.value(role, "role");
    }

    /**
     * @param expected role to check
     * @return true if the user plays {@code expected}
     */
    public boolean hasRole(Role expected) {
        return role == expected;
    }

    /** @return identity, or null if not persisted yet */
    public Long getId() {
        return id;
    }

    /** @param id identity assigned by the repository */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return unique login name */
    public String getUsername() {
        return username;
    }

    /** @return full name */
    public String getFullName() {
        return fullName;
    }

    /** @return email address */
    public String getEmail() {
        return email;
    }

    /** @return role played in the system */
    public Role getRole() {
        return role;
    }
}
