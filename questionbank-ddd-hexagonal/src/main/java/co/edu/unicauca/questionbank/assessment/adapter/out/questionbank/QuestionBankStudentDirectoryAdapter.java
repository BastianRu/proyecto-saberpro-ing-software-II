package co.edu.unicauca.questionbank.assessment.adapter.out.questionbank;

import co.edu.unicauca.questionbank.assessment.application.port.out.StudentDirectoryPort;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.StudentId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.StudentProfile;
import co.edu.unicauca.questionbank.domain.model.Role;
import co.edu.unicauca.questionbank.domain.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Secondary (driven) adapter that looks up students among the users of the
 * question bank module (Workshop 10) and translates them into a
 * {@link StudentProfile}. The assessment core never sees the {@code Role} enum.
 */
@Component
public class QuestionBankStudentDirectoryAdapter implements StudentDirectoryPort {

    private final UserRepository users;

    /** @param users user repository contract of the question bank */
    public QuestionBankStudentDirectoryAdapter(UserRepository users) {
        this.users = users;
    }

    @Override
    public Optional<StudentProfile> findById(StudentId id) {
        return users.findById(id.value())
                .map(user -> new StudentProfile(id, user.getFullName(), user.hasRole(Role.STUDENT)));
    }
}
