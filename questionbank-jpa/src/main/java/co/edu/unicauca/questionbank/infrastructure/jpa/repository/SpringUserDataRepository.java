package co.edu.unicauca.questionbank.infrastructure.jpa.repository;

import co.edu.unicauca.questionbank.infrastructure.jpa.record.UserRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data interface used internally by the user repository implementation. */
public interface SpringUserDataRepository extends JpaRepository<UserRecord, Long> {

    /**
     * @param username login name
     * @return true if a user already uses that name
     */
    boolean existsByUsername(String username);
}
