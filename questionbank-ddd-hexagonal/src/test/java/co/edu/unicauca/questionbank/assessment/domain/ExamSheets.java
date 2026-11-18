package co.edu.unicauca.questionbank.assessment.domain;

import co.edu.unicauca.questionbank.assessment.domain.valueobject.AnswerValue;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.ExamChoice;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.ExamItem;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.ExamSheet;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.ItemKind;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.QuestionId;
import co.edu.unicauca.questionbank.assessment.domain.valueobject.QuizId;

import java.time.Duration;
import java.util.List;

/** Test fixtures: an exam with one multiple choice (1 point) and one true/false question (2 points). */
public final class ExamSheets {

    /** Multiple choice question; correct answer B. */
    public static final QuestionId MC = QuestionId.of(10L);
    /** True/false question; correct answer FALSE. */
    public static final QuestionId TF = QuestionId.of(11L);
    /** Quiz of the sample sheet. */
    public static final QuizId QUIZ = QuizId.of(1L);

    private ExamSheets() {
    }

    /** @return the sample sheet with a ten minute time limit */
    public static ExamSheet sample() {
        ExamItem mc = new ExamItem(MC, 1, 1.0, "Which pattern organizes a system in tiers?",
                ItemKind.MULTIPLE_CHOICE,
                List.of(choice("A", "Microkernel"), choice("B", "Layers"), choice("C", "Broker"),
                        choice("D", "Pipes and filters"), choice("E", "Peer to peer")),
                AnswerValue.of("B"));
        ExamItem tf = new ExamItem(TF, 2, 2.0, "Domain entities must carry JPA annotations.", ItemKind.TRUE_FALSE,
                List.of(choice("TRUE", "True"), choice("FALSE", "False")), AnswerValue.of("FALSE"));
        return new ExamSheet(QUIZ, "Architecture mock exam", List.of(mc, tf), Duration.ofMinutes(10));
    }

    /** @return a sheet without questions */
    public static ExamSheet empty() {
        return new ExamSheet(QUIZ, "Empty", List.of(), Duration.ZERO);
    }

    private static ExamChoice choice(String key, String text) {
        return new ExamChoice(AnswerValue.of(key), text);
    }
}
