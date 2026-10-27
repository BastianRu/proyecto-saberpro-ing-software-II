package co.edu.unicauca.questionbank.api.dto;

import co.edu.unicauca.questionbank.domain.model.CognitiveLevel;
import co.edu.unicauca.questionbank.domain.model.Difficulty;
import co.edu.unicauca.questionbank.domain.model.QuestionType;

import java.util.List;
import java.util.Set;

/**
 * Request body to create a question. {@code options} is used by multiple choice
 * questions and {@code correctAnswer} by true/false questions.
 *
 * @param type          kind of question
 * @param statement     statement
 * @param difficulty    difficulty
 * @param authorId      author identifier
 * @param options       answer options (multiple choice only)
 * @param correctAnswer expected truth value (true/false only)
 * @param metadata      optional metadata
 * @param topicId       optional topic
 * @param competencyIds optional competencies
 */
public record CreateQuestionRequest(QuestionType type, String statement, Difficulty difficulty, Long authorId,
                                    List<Option> options, Boolean correctAnswer, Metadata metadata,
                                    Long topicId, Set<Long> competencyIds) {

    /**
     * An answer option.
     *
     * @param text    option text
     * @param correct true if correct
     */
    public record Option(String text, boolean correct) {
    }

    /**
     * Optional editorial metadata.
     *
     * @param cognitiveLevel       cognitive level
     * @param estimatedTimeSeconds expected time in seconds
     * @param editorialNotes       editorial notes
     */
    public record Metadata(CognitiveLevel cognitiveLevel, Integer estimatedTimeSeconds, String editorialNotes) {
    }
}
