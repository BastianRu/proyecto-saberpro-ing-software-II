package co.edu.unicauca.questionbank.api.dto;

import co.edu.unicauca.questionbank.domain.model.AnswerOption;
import co.edu.unicauca.questionbank.domain.model.CognitiveLevel;
import co.edu.unicauca.questionbank.domain.model.Difficulty;
import co.edu.unicauca.questionbank.domain.model.MultipleChoiceQuestion;
import co.edu.unicauca.questionbank.domain.model.Question;
import co.edu.unicauca.questionbank.domain.model.QuestionMetadata;
import co.edu.unicauca.questionbank.domain.model.QuestionStatus;
import co.edu.unicauca.questionbank.domain.model.QuestionType;
import co.edu.unicauca.questionbank.domain.model.TrueFalseQuestion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * JSON representation of a question of any type.
 *
 * @param id              identifier
 * @param type            kind of question
 * @param statement       statement
 * @param difficulty      difficulty
 * @param status          lifecycle status
 * @param authorId        author identifier
 * @param topicId         topic identifier, or null
 * @param competencyIds   competency identifiers
 * @param options         options (multiple choice), or null
 * @param correctAnswer   expected truth value (true/false), or null
 * @param metadata        metadata
 * @param rejectionReason last rejection reason, or null
 */
public record QuestionResponse(Long id, QuestionType type, String statement, Difficulty difficulty,
                               QuestionStatus status, Long authorId, Long topicId, Set<Long> competencyIds,
                               List<OptionResponse> options, Boolean correctAnswer, MetadataResponse metadata,
                               String rejectionReason) {

    /**
     * @param question domain question
     * @return its JSON representation
     */
    public static QuestionResponse from(Question question) {
        List<OptionResponse> options = null;
        Boolean correctAnswer = null;
        if (question instanceof MultipleChoiceQuestion mc) {
            options = mc.getOptions().stream().map(OptionResponse::from).toList();
        } else if (question instanceof TrueFalseQuestion tf) {
            correctAnswer = tf.getCorrectAnswer();
        }
        return new QuestionResponse(question.getId(), question.getType(), question.getStatement(),
                question.getDifficulty(), question.getStatus(), question.getAuthorId(), question.getTopicId(),
                question.getCompetencyIds(), options, correctAnswer, MetadataResponse.from(question.getMetadata()),
                question.getRejectionReason());
    }

    /**
     * An answer option.
     *
     * @param id      identifier
     * @param text    text
     * @param correct true if correct
     */
    public record OptionResponse(Long id, String text, boolean correct) {

        static OptionResponse from(AnswerOption option) {
            return new OptionResponse(option.getId(), option.getText(), option.isCorrect());
        }
    }

    /**
     * Editorial metadata.
     *
     * @param cognitiveLevel       cognitive level
     * @param estimatedTimeSeconds expected time in seconds
     * @param editorialNotes       editorial notes
     * @param lastReviewedAt       date of the last review
     */
    public record MetadataResponse(CognitiveLevel cognitiveLevel, int estimatedTimeSeconds,
                                   String editorialNotes, LocalDateTime lastReviewedAt) {

        static MetadataResponse from(QuestionMetadata metadata) {
            return new MetadataResponse(metadata.getCognitiveLevel(), metadata.getEstimatedTimeSeconds(),
                    metadata.getEditorialNotes(), metadata.getLastReviewedAt());
        }
    }
}
