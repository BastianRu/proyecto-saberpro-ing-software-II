package co.edu.unicauca.questionbank.assessment.domain.valueobject;

/**
 * Result of a submitted attempt.
 *
 * @param earnedPoints   points obtained with correct answers
 * @param totalPoints    maximum points of the exam
 * @param correctAnswers number of correct answers
 * @param totalQuestions number of questions in the exam
 */
public record Score(double earnedPoints, double totalPoints, int correctAnswers, int totalQuestions) {

    /** Ensures the score is consistent. */
    public Score {
        if (earnedPoints < 0 || totalPoints < 0 || earnedPoints > totalPoints) {
            throw new IllegalArgumentException("Earned points must be between 0 and the total points");
        }
        if (correctAnswers < 0 || correctAnswers > totalQuestions) {
            throw new IllegalArgumentException("Correct answers must be between 0 and the number of questions");
        }
    }

    /** @return earned points as a percentage of the total, rounded to one decimal */
    public double percentage() {
        if (totalPoints == 0) {
            return 0;
        }
        return Math.round(earnedPoints * 1000.0 / totalPoints) / 10.0;
    }
}
