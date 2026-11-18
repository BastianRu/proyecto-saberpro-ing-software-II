package co.edu.unicauca.questionbank.assessment.domain.valueobject;

/**
 * What the assessment context needs to know about a person who wants to take a
 * mock exam. It is translated from the user of the question bank context.
 *
 * @param id             student identity
 * @param fullName       full name
 * @param hasStudentRole true if the person plays the STUDENT role
 */
public record StudentProfile(StudentId id, String fullName, boolean hasStudentRole) {

    /** Validates the profile. */
    public StudentProfile {
        if (id == null || fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Student profile data is required");
        }
    }
}
