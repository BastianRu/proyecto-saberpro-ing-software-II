package co.edu.unicauca.questionbank.assessment.domain.valueobject;

import java.util.List;

/**
 * A question of a mock exam, translated from the question bank into the
 * language of the assessment context. It carries the answer key, which is only
 * revealed to the student after the attempt is submitted.
 *
 * @param questionId    question identity in the question bank
 * @param position      order inside the exam, starting at 1
 * @param points        points awarded for a correct answer
 * @param statement     statement shown to the student
 * @param kind          kind of question
 * @param choices       choices shown to the student
 * @param correctAnswer key of the correct choice
 */
public record ExamItem(QuestionId questionId, int position, double points, String statement,
                       ItemKind kind, List<ExamChoice> choices, AnswerValue correctAnswer) {

    /** Validates the item and makes the choices immutable. */
    public ExamItem {
        if (questionId == null || kind == null || correctAnswer == null || statement == null) {
            throw new IllegalArgumentException("Exam item data is required");
        }
        if (points <= 0) {
            throw new IllegalArgumentException("Points must be positive");
        }
        choices = choices == null ? List.of() : List.copyOf(choices);
        if (choices.stream().noneMatch(c -> c.key().equals(correctAnswer))) {
            throw new IllegalArgumentException("The correct answer must be one of the choices");
        }
    }

    /**
     * @param answer answer sent by the student
     * @return true if the answer is one of the choices of this item
     */
    public boolean accepts(AnswerValue answer) {
        return choices.stream().anyMatch(c -> c.key().equals(answer));
    }

    /**
     * @param answer answer sent by the student
     * @return true if it matches the answer key
     */
    public boolean isCorrect(AnswerValue answer) {
        return correctAnswer.equals(answer);
    }
}
