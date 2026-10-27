package co.edu.unicauca.questionbank.domain.model;

/**
 * A question placed in a quiz, with its position and the points it is worth.
 *
 * @param questionId identifier of the question
 * @param position   order of the question inside the quiz, starting at 1
 * @param points     points awarded for a correct answer
 */
public record QuizItem(Long questionId, int position, double points) {
}
