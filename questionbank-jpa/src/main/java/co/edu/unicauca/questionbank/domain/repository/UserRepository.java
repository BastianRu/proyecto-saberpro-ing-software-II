package co.edu.unicauca.questionbank.domain.repository;

import co.edu.unicauca.questionbank.domain.model.User;

import java.util.List;
import java.util.Optional;

/** Persistence contract for users. */
public interface UserRepository {

    /**
     * @param user user to store
     * @return the stored user with its identity
     */
    User save(User user);

    /**
     * @param id user identifier
     * @return the user, if it exists
     */
    Optional<User> findById(Long id);

    /** @return every user */
    List<User> findAll();

    /**
     * @param username login name
     * @return true if a user already uses that name
     */
    boolean existsByUsername(String username);
}
