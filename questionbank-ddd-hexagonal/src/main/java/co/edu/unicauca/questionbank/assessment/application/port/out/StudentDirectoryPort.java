package co.edu.unicauca.questionbank.assessment.application.port.out;

import co.edu.unicauca.questionbank.assessment.domain.valueobject.StudentId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.StudentProfile;

import java.util.Optional;

/** Secondary (outbound) port to look up who is taking an exam. */
public interface StudentDirectoryPort {

    /**
     * @param id person identity
     * @return the profile, if the person exists
     */
    Optional<StudentProfile> findById(StudentId id);
}
