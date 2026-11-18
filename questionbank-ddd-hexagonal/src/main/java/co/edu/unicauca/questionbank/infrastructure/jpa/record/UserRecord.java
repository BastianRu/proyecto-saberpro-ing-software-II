package co.edu.unicauca.questionbank.infrastructure.jpa.record;

import co.edu.unicauca.questionbank.domain.model.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** JPA record of a user (table {@code users}). */
@Entity
@Table(name = "users")
public class UserRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** Required by JPA. */
    protected UserRecord() {
    }

    /**
     * Creates a record.
     *
     * @param username login name
     * @param fullName full name
     * @param email    email
     * @param role     role
     */
    public UserRecord(String username, String fullName, String email, Role role) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return login name */
    public String getUsername() {
        return username;
    }

    /** @return full name */
    public String getFullName() {
        return fullName;
    }

    /** @return email */
    public String getEmail() {
        return email;
    }

    /** @return role */
    public Role getRole() {
        return role;
    }
}
