package co.edu.unicauca.questionbank.infrastructure.jpa;

import co.edu.unicauca.questionbank.domain.model.Quiz;
import co.edu.unicauca.questionbank.domain.model.QuizItem;
import co.edu.unicauca.questionbank.domain.repository.QuizRepository;
import co.edu.unicauca.questionbank.infrastructure.jpa.mapper.QuizMapper;
import co.edu.unicauca.questionbank.infrastructure.jpa.record.QuestionRecord;
import co.edu.unicauca.questionbank.infrastructure.jpa.record.QuizQuestionRecord;
import co.edu.unicauca.questionbank.infrastructure.jpa.record.QuizRecord;
import co.edu.unicauca.questionbank.infrastructure.jpa.record.UserRecord;
import co.edu.unicauca.questionbank.infrastructure.jpa.repository.SpringQuestionDataRepository;
import co.edu.unicauca.questionbank.infrastructure.jpa.repository.SpringQuizDataRepository;
import co.edu.unicauca.questionbank.infrastructure.jpa.repository.SpringUserDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** JPA implementation of the {@link QuizRepository} contract. */
@Repository
@Transactional(readOnly = true)
public class QuizRepositoryJpa implements QuizRepository {

    private final SpringQuizDataRepository quizzes;
    private final SpringQuestionDataRepository questions;
    private final SpringUserDataRepository users;

    /**
     * Creates the repository.
     *
     * @param quizzes   Spring Data quiz repository
     * @param questions Spring Data question repository
     * @param users     Spring Data user repository
     */
    public QuizRepositoryJpa(SpringQuizDataRepository quizzes, SpringQuestionDataRepository questions,
                             SpringUserDataRepository users) {
        this.quizzes = quizzes;
        this.questions = questions;
        this.users = users;
    }

    @Override
    @Transactional
    public Quiz save(Quiz quiz) {
        if (quiz.getId() != null) {
            throw new IllegalStateException("Quizzes cannot be modified once created");
        }
        UserRecord teacher = users.findById(quiz.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + quiz.getTeacherId()));
        QuizRecord record = new QuizRecord(quiz.getTitle(), teacher, quiz.getCreatedAt());
        for (QuizItem item : quiz.getItems()) {
            QuestionRecord question = questions.findById(item.questionId())
                    .orElseThrow(() -> new IllegalArgumentException("Question not found: " + item.questionId()));
            record.addQuestion(new QuizQuestionRecord(question, item.position(), item.points()));
        }
        return QuizMapper.toDomain(quizzes.save(record));
    }

    @Override
    public Optional<Quiz> findById(Long id) {
        return quizzes.findById(id).map(QuizMapper::toDomain);
    }

    @Override
    public List<Quiz> findAll() {
        return quizzes.findAll().stream().map(QuizMapper::toDomain).toList();
    }
}
