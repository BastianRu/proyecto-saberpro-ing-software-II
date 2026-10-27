package co.edu.unicauca.questionbank.application;

import co.edu.unicauca.questionbank.domain.exception.BusinessRuleException;
import co.edu.unicauca.questionbank.domain.model.Question;
import co.edu.unicauca.questionbank.domain.model.Role;
import co.edu.unicauca.questionbank.domain.model.User;
import co.edu.unicauca.questionbank.domain.repository.QuestionRepository;
import co.edu.unicauca.questionbank.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Use cases of the reviewer: take a pending question, approve it or reject it
 * with a reason. A reviewer can never review their own question.
 */
@Service
@Transactional
public class ReviewService {

    private final QuestionRepository questions;
    private final UserRepository users;

    /**
     * Creates the service.
     *
     * @param questions question repository contract
     * @param users     user repository contract
     */
    public ReviewService(QuestionRepository questions, UserRepository users) {
        this.questions = questions;
        this.users = users;
    }

    /**
     * PENDING_REVIEW -> IN_REVIEW.
     *
     * @param questionId question identifier
     * @param reviewerId reviewer identifier
     * @return the updated question
     */
    public Question startReview(Long questionId, Long reviewerId) {
        Question question = loadForReviewer(questionId, reviewerId);
        question.startReview();
        return questions.save(question);
    }

    /**
     * IN_REVIEW -> APPROVED.
     *
     * @param questionId question identifier
     * @param reviewerId reviewer identifier
     * @return the updated question
     */
    public Question approve(Long questionId, Long reviewerId) {
        Question question = loadForReviewer(questionId, reviewerId);
        question.approve(LocalDateTime.now());
        return questions.save(question);
    }

    /**
     * IN_REVIEW -> REJECTED, storing the reason.
     *
     * @param questionId question identifier
     * @param reviewerId reviewer identifier
     * @param reason     reason of the rejection
     * @return the updated question
     */
    public Question reject(Long questionId, Long reviewerId, String reason) {
        Question question = loadForReviewer(questionId, reviewerId);
        question.reject(reason, LocalDateTime.now());
        return questions.save(question);
    }

    private Question loadForReviewer(Long questionId, Long reviewerId) {
        User reviewer = users.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", reviewerId));
        if (!reviewer.hasRole(Role.REVIEWER)) {
            throw new BusinessRuleException("User " + reviewerId + " is not a REVIEWER");
        }
        Question question = questions.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", questionId));
        if (question.getAuthorId().equals(reviewerId)) {
            throw new BusinessRuleException("A reviewer cannot review their own question");
        }
        return question;
    }
}
