package co.edu.unicauca.questionbank.domain.model;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Multiple choice question with a single correct answer. Following the
 * academic rules of the bank it has exactly four distractors and one correct
 * answer.
 */
public class MultipleChoiceQuestion extends Question {

    /** Number of options required: four distractors plus the correct answer. */
    public static final int REQUIRED_OPTIONS = 5;

    private final List<AnswerOption> options;

    /**
     * Rebuilds a multiple choice question (used by repositories).
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
     * @param options         answer options in display order
     */
    public MultipleChoiceQuestion(Long id, String statement, Difficulty difficulty, QuestionStatus status,
                                  Long authorId, Long topicId, Set<Long> competencyIds,
                                  QuestionMetadata metadata, String rejectionReason,
                                  List<AnswerOption> options) {
        super(id, statement, difficulty, status, authorId, topicId, competencyIds, metadata, rejectionReason);
        this.options = options == null ? List.of() : List.copyOf(options);
    }

    /**
     * Creates a new draft and validates its content.
     *
     * @param statement  statement
     * @param difficulty difficulty
     * @param authorId   author identifier
     * @param options    the five answer options
     * @param metadata   metadata, or null for defaults
     * @return the new draft question
     * @throws co.edu.unicauca.questionbank.domain.exception.InvalidQuestionException if the content is invalid
     */
    public static MultipleChoiceQuestion draft(String statement, Difficulty difficulty, Long authorId,
                                               List<AnswerOption> options, QuestionMetadata metadata) {
        MultipleChoiceQuestion question = new MultipleChoiceQuestion(null, statement, difficulty,
                QuestionStatus.DRAFT, authorId, null, Set.of(), metadata, null, options);
        question.validateContent();
        return question;
    }

    @Override
    public QuestionType getType() {
        return QuestionType.MULTIPLE_CHOICE;
    }

    @Override
    protected void collectContentErrors(List<String> errors) {
        if (options.size() != REQUIRED_OPTIONS) {
            errors.add("A multiple choice question needs exactly " + REQUIRED_OPTIONS
                    + " options (4 distractors and 1 correct answer), found " + options.size());
        }
        long correct = options.stream().filter(AnswerOption::isCorrect).count();
        if (correct != 1) {
            errors.add("Exactly one option must be correct, found " + correct);
        }
        if (options.stream().anyMatch(o -> o.getText().isBlank())) {
            errors.add("Options cannot be blank");
        }
        Set<String> seen = new HashSet<>();
        for (AnswerOption option : options) {
            if (!option.getText().isBlank() && !seen.add(option.getText().toLowerCase(Locale.ROOT))) {
                errors.add("Duplicated option: " + option.getText());
            }
        }
    }

    /** @return answer options in display order (read-only) */
    public List<AnswerOption> getOptions() {
        return options;
    }

    /** @return the correct option */
    public AnswerOption getCorrectOption() {
        return options.stream().filter(AnswerOption::isCorrect).findFirst()
                .orElseThrow(() -> new IllegalStateException("Question without correct option"));
    }
}
