package co.edu.unicauca.questionbank.domain;

import co.edu.unicauca.questionbank.domain.exception.BusinessRuleException;
import co.edu.unicauca.questionbank.domain.exception.InvalidQuestionException;
import co.edu.unicauca.questionbank.domain.exception.InvalidStatusTransitionException;
import co.edu.unicauca.questionbank.domain.model.Question;
import co.edu.unicauca.questionbank.domain.model.QuestionStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pure domain tests of the question lifecycle (no Spring, no JPA). */
class QuestionLifecycleTest {

    private Question classifiedDraft() {
        Question question = SampleQuestions.multipleChoice(1L);
        question.classify(10L, Set.of(20L));
        return question;
    }

    @Test
    void newQuestionStartsAsDraft() {
        assertEquals(QuestionStatus.DRAFT, SampleQuestions.trueFalse(1L).getStatus());
    }

    @Test
    void completeLifecycleReachesArchived() {
        Question question = classifiedDraft();
        question.submitForReview();
        question.startReview();
        question.approve(LocalDateTime.now());
        assertNotNull(question.getMetadata().getLastReviewedAt());
        question.publish();
        assertTrue(question.isAvailableForQuizzes());
        question.archive();
        assertEquals(QuestionStatus.ARCHIVED, question.getStatus());
        assertFalse(question.isAvailableForQuizzes());
    }

    @Test
    void submitRequiresClassification() {
        Question question = SampleQuestions.multipleChoice(1L);
        InvalidQuestionException ex = assertThrows(InvalidQuestionException.class, question::submitForReview);
        assertTrue(ex.getErrors().get(0).contains("topic"));
        assertEquals(QuestionStatus.DRAFT, question.getStatus());
    }

    @Test
    void draftCannotBeApprovedOrPublished() {
        Question question = classifiedDraft();
        assertThrows(InvalidStatusTransitionException.class, () -> question.approve(LocalDateTime.now()));
        assertThrows(InvalidStatusTransitionException.class, question::publish);
    }

    @Test
    void rejectedQuestionStoresReasonAndCanBeCorrected() {
        Question question = classifiedDraft();
        question.submitForReview();
        question.startReview();
        question.reject("Ambiguous statement", LocalDateTime.now());
        assertEquals(QuestionStatus.REJECTED, question.getStatus());
        assertEquals("Ambiguous statement", question.getRejectionReason());

        question.reopenForCorrection();
        question.submitForReview();
        assertEquals(QuestionStatus.PENDING_REVIEW, question.getStatus());
    }

    @Test
    void rejectionNeedsAReason() {
        Question question = classifiedDraft();
        question.submitForReview();
        question.startReview();
        assertThrows(BusinessRuleException.class, () -> question.reject(" ", LocalDateTime.now()));
        assertEquals(QuestionStatus.IN_REVIEW, question.getStatus());
    }

    @Test
    void onlyDraftsCanBeClassified() {
        Question question = classifiedDraft();
        question.submitForReview();
        assertThrows(BusinessRuleException.class, () -> question.classify(11L, Set.of(21L)));
    }
}
