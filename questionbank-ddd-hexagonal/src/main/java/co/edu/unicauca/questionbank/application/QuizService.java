package co.edu.unicauca.questionbank.application;

import co.edu.unicauca.questionbank.domain.exception.BusinessRuleException;
import co.edu.unicauca.questionbank.domain.model.Question;
import co.edu.unicauca.questionbank.domain.model.Quiz;
import co.edu.unicauca.questionbank.domain.model.Role;
import co.edu.unicauca.questionbank.domain.model.User;
import co.edu.unicauca.questionbank.domain.repository.QuestionRepository;
import co.edu.unicauca.questionbank.domain.repository.QuizRepository;
import co.edu.unicauca.questionbank.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Use case 5: a teacher creates a quiz with published questions. The whole
 * operation runs in one transaction, so an invalid question leaves nothing stored.
 */
@Service
@Transactional
public class QuizService {

    /** Points assigned to every question when the teacher does not specify them. */
    public static final double DEFAULT_POINTS = 1.0;

    private final QuizRepository quizzes;
    private final QuestionRepository questions;
    private final UserRepository users;

    /**
     * Creates the service.
     *
     * @param quizzes   quiz repository contract
     * @param questions question repository contract
     * @param users     user repository contract
     */
    public QuizService(QuizRepository quizzes, QuestionRepository questions, UserRepository users) {
        this.quizzes = quizzes;
        this.questions = questions;
        this.users = users;
    }

    /**
     * Creates a quiz.
     *
     * @param title       title
     * @param teacherId   teacher who organizes it (must have the TEACHER role)
     * @param questionIds questions to include, in order; all must be PUBLISHED
     * @return the stored quiz
     * @throws BusinessRuleException if any question is not published or is repeated
     */
    public Quiz createQuiz(String title, Long teacherId, List<Long> questionIds) {
        User teacher = users.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("User", teacherId));
        if (!teacher.hasRole(Role.TEACHER)) {
            throw new BusinessRuleException("User " + teacherId + " is not a TEACHER");
        }
        if (questionIds == null || questionIds.isEmpty()) {
            throw new BusinessRuleException("A quiz needs at least one question");
        }
        Quiz quiz = new Quiz(null, title, teacherId, LocalDateTime.now(), List.of());
        for (Long questionId : questionIds) {
            Question question = questions.findById(questionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Question", questionId));
            quiz.addQuestion(question, DEFAULT_POINTS);
        }
        return quizzes.save(quiz);
    }

    /**
     * @param id quiz identifier
     * @return the quiz
     */
    @Transactional(readOnly = true)
    public Quiz findById(Long id) {
        return quizzes.findById(id).orElseThrow(() -> new ResourceNotFoundException("Quiz", id));
    }

    /** @return every quiz */
    @Transactional(readOnly = true)
    public List<Quiz> findAll() {
        return quizzes.findAll();
    }
}
