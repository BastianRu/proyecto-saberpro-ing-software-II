package co.edu.unicauca.questionbank.domain;

import co.edu.unicauca.questionbank.domain.model.AnswerOption;
import co.edu.unicauca.questionbank.domain.model.Difficulty;
import co.edu.unicauca.questionbank.domain.model.MultipleChoiceQuestion;
import co.edu.unicauca.questionbank.domain.model.TrueFalseQuestion;

import java.util.List;

/** Builders of valid questions shared by the tests. */
public final class SampleQuestions {

    private SampleQuestions() {
    }

    /** @return four distractors and one correct answer */
    public static List<AnswerOption> fiveOptions() {
        return List.of(AnswerOption.of("Singleton", false), AnswerOption.of("Layers", true),
                AnswerOption.of("Observer", false), AnswerOption.of("Adapter", false),
                AnswerOption.of("Facade", false));
    }

    /**
     * @param authorId author identifier
     * @return a valid multiple choice draft
     */
    public static MultipleChoiceQuestion multipleChoice(Long authorId) {
        return MultipleChoiceQuestion.draft("Which pattern splits a system into tiers?", Difficulty.LOW,
                authorId, fiveOptions(), null);
    }

    /**
     * @param authorId author identifier
     * @return a valid true/false draft
     */
    public static TrueFalseQuestion trueFalse(Long authorId) {
        return TrueFalseQuestion.draft("JPA annotations belong to the domain model.", Difficulty.MEDIUM,
                authorId, false, null);
    }
}
