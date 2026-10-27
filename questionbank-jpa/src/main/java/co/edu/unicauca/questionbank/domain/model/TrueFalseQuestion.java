package co.edu.unicauca.questionbank.domain.model;

import java.util.List;
import java.util.Set;

/** True/false question: the student decides whether the statement is true. */
public class TrueFalseQuestion extends Question {

    private final boolean correctAnswer;

    /**
     * Rebuilds a true/false question (used by repositories).
     *
     * @param id              identity
     * @param statement       statement
     * @param difficulty      difficulty
     * @param status          lifecycle status
     * @param authorId        author identifier
     * @param topicId         topic identifier, may be null
     * @param competencyIds   competency identifiers
     * @param metadata        metadata
     * @param rejectionReason last rejection reason, may be null
     * @param correctAnswer   expected truth value
     */
    public TrueFalseQuestion(Long id, String statement, Difficulty difficulty, QuestionStatus status,
                             Long authorId, Long topicId, Set<Long> competencyIds,
                             QuestionMetadata metadata, String rejectionReason, boolean correctAnswer) {
        super(id, statement, difficulty, status, authorId, topicId, competencyIds, metadata, rejectionReason);
        this.correctAnswer = correctAnswer;
    }

    /**
     * Creates a new draft and validates its content.
     *
     * @param statement     statement
     * @param difficulty    difficulty
     * @param authorId      author identifier
     * @param correctAnswer expected truth value
     * @param metadata      metadata, or null for defaults
     * @return the new draft question
     */
    public static TrueFalseQuestion draft(String statement, Difficulty difficulty, Long authorId,
                                          boolean correctAnswer, QuestionMetadata metadata) {
        TrueFalseQuestion question = new TrueFalseQuestion(null, statement, difficulty,
                QuestionStatus.DRAFT, authorId, null, Set.of(), metadata, null, correctAnswer);
        question.validateContent();
        return question;
    }

    @Override
    public QuestionType getType() {
        return QuestionType.TRUE_FALSE;
    }

    @Override
    protected void collectContentErrors(List<String> errors) {
        // The statement is the only content; it is checked by the base class.
    }

    /** @return expected truth value */
    public boolean getCorrectAnswer() {
        return correctAnswer;
    }
}
