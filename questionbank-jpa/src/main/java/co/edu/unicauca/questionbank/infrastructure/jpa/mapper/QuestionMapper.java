package co.edu.unicauca.questionbank.infrastructure.jpa.mapper;

import co.edu.unicauca.questionbank.domain.model.AnswerOption;
import co.edu.unicauca.questionbank.domain.model.MultipleChoiceQuestion;
import co.edu.unicauca.questionbank.domain.model.Question;
import co.edu.unicauca.questionbank.domain.model.QuestionMetadata;
import co.edu.unicauca.questionbank.domain.model.TrueFalseQuestion;
import co.edu.unicauca.questionbank.infrastructure.jpa.record.AnswerOptionRecord;
import co.edu.unicauca.questionbank.infrastructure.jpa.record.CompetencyRecord;
import co.edu.unicauca.questionbank.infrastructure.jpa.record.MultipleChoiceQuestionRecord;
import co.edu.unicauca.questionbank.infrastructure.jpa.record.QuestionMetadataRecord;
import co.edu.unicauca.questionbank.infrastructure.jpa.record.QuestionRecord;
import co.edu.unicauca.questionbank.infrastructure.jpa.record.TopicRecord;
import co.edu.unicauca.questionbank.infrastructure.jpa.record.TrueFalseQuestionRecord;
import co.edu.unicauca.questionbank.infrastructure.jpa.record.UserRecord;
import org.hibernate.Hibernate;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Translates between the domain question hierarchy and the JPA record
 * hierarchy. This is the only place that knows both models.
 */
public final class QuestionMapper {

    private QuestionMapper() {
    }

    /**
     * Converts a record (of any subtype) to its domain question. Must be called
     * inside a transaction because it reads lazy associations.
     *
     * @param record JPA record, possibly a Hibernate proxy
     * @return domain question of the matching subtype
     */
    public static Question toDomain(QuestionRecord record) {
        QuestionRecord real = (QuestionRecord) Hibernate.unproxy(record);
        Long topicId = real.getTopic() == null ? null : real.getTopic().getId();
        Set<Long> competencyIds = real.getCompetencies().stream()
                .map(CompetencyRecord::getId).collect(Collectors.toSet());
        QuestionMetadata metadata = toDomain(real.getMetadata());

        if (real instanceof MultipleChoiceQuestionRecord mc) {
            List<AnswerOption> options = mc.getOptions().stream()
                    .map(o -> new AnswerOption(o.getId(), o.getText(), o.isCorrect()))
                    .toList();
            return new MultipleChoiceQuestion(real.getId(), real.getStatement(), real.getDifficulty(),
                    real.getStatus(), real.getAuthor().getId(), topicId, competencyIds, metadata,
                    real.getRejectionReason(), options);
        }
        if (real instanceof TrueFalseQuestionRecord tf) {
            return new TrueFalseQuestion(real.getId(), real.getStatement(), real.getDifficulty(),
                    real.getStatus(), real.getAuthor().getId(), topicId, competencyIds, metadata,
                    real.getRejectionReason(), tf.getCorrectAnswer());
        }
        throw new IllegalStateException("Unknown question record: " + real.getClass().getName());
    }

    /**
     * Creates an empty record of the subtype that matches the domain question.
     *
     * @param question domain question
     * @return new, empty record
     */
    public static QuestionRecord newRecord(Question question) {
        return switch (question.getType()) {
            case MULTIPLE_CHOICE -> new MultipleChoiceQuestionRecord();
            case TRUE_FALSE -> new TrueFalseQuestionRecord();
        };
    }

    /**
     * Copies the state of a domain question into a (new or managed) record.
     * The referenced records are resolved by the repository beforehand.
     *
     * @param question     domain question
     * @param record       target record of the same subtype
     * @param author       author record
     * @param topic        topic record, or null
     * @param competencies competency records
     */
    public static void copyToRecord(Question question, QuestionRecord record, UserRecord author,
                                    TopicRecord topic, Set<CompetencyRecord> competencies) {
        record.setStatement(question.getStatement());
        record.setDifficulty(question.getDifficulty());
        record.setStatus(question.getStatus());
        record.setAuthor(author);
        record.setTopic(topic);
        record.replaceCompetencies(competencies);
        record.setRejectionReason(question.getRejectionReason());
        copyMetadata(question.getMetadata(), record);

        if (question instanceof MultipleChoiceQuestion mc && record instanceof MultipleChoiceQuestionRecord mcr) {
            syncOptions(mc.getOptions(), mcr);
        } else if (question instanceof TrueFalseQuestion tf && record instanceof TrueFalseQuestionRecord tfr) {
            tfr.setCorrectAnswer(tf.getCorrectAnswer());
        } else {
            throw new IllegalStateException("The type of question " + question.getId() + " cannot change");
        }
    }

    private static QuestionMetadata toDomain(QuestionMetadataRecord record) {
        if (record == null) {
            return null;
        }
        return new QuestionMetadata(record.getCognitiveLevel(), record.getEstimatedTimeSeconds(),
                record.getEditorialNotes(), record.getLastReviewedAt());
    }

    private static void copyMetadata(QuestionMetadata metadata, QuestionRecord record) {
        QuestionMetadataRecord target = record.getMetadata();
        if (target == null) {
            target = new QuestionMetadataRecord();
            record.setMetadata(target);
        }
        target.setCognitiveLevel(metadata.getCognitiveLevel());
        target.setEstimatedTimeSeconds(metadata.getEstimatedTimeSeconds());
        target.setEditorialNotes(metadata.getEditorialNotes());
        target.setLastReviewedAt(metadata.getLastReviewedAt());
    }

    /**
     * Updates the options in place instead of replacing the collection: Hibernate
     * flushes inserts before deletes, so replacing them would temporarily break the
     * (question_id, position) unique constraint.
     */
    private static void syncOptions(List<AnswerOption> options, MultipleChoiceQuestionRecord record) {
        List<AnswerOptionRecord> current = record.getOptions();
        for (int i = 0; i < options.size(); i++) {
            AnswerOption option = options.get(i);
            if (i < current.size()) {
                current.get(i).setText(option.getText());
                current.get(i).setCorrect(option.isCorrect());
            } else {
                record.addOption(new AnswerOptionRecord(option.getText(), option.isCorrect(), i + 1));
            }
        }
        while (current.size() > options.size()) {
            record.removeOption(current.get(current.size() - 1));
        }
    }
}
