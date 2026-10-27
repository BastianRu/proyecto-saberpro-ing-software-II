package co.edu.unicauca.questionbank.infrastructure.jpa;

import co.edu.unicauca.questionbank.domain.SampleQuestions;
import co.edu.unicauca.questionbank.domain.model.AnswerOption;
import co.edu.unicauca.questionbank.domain.model.CognitiveLevel;
import co.edu.unicauca.questionbank.domain.model.Competency;
import co.edu.unicauca.questionbank.domain.model.Difficulty;
import co.edu.unicauca.questionbank.domain.model.MultipleChoiceQuestion;
import co.edu.unicauca.questionbank.domain.model.Question;
import co.edu.unicauca.questionbank.domain.model.QuestionMetadata;
import co.edu.unicauca.questionbank.domain.model.QuestionStatus;
import co.edu.unicauca.questionbank.domain.model.Quiz;
import co.edu.unicauca.questionbank.domain.model.Role;
import co.edu.unicauca.questionbank.domain.model.Topic;
import co.edu.unicauca.questionbank.domain.model.TrueFalseQuestion;
import co.edu.unicauca.questionbank.domain.model.User;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests of the JPA repositories against H2. Every check clears the
 * persistence context first, so the data is really read back from the database.
 */
@DataJpaTest
@Import({QuestionRepositoryJpa.class, UserRepositoryJpa.class, TopicRepositoryJpa.class,
        CompetencyRepositoryJpa.class, QuizRepositoryJpa.class})
class QuestionRepositoryJpaTest {

    @Autowired
    private QuestionRepositoryJpa questions;
    @Autowired
    private UserRepositoryJpa users;
    @Autowired
    private TopicRepositoryJpa topics;
    @Autowired
    private CompetencyRepositoryJpa competencies;
    @Autowired
    private QuizRepositoryJpa quizzes;
    @Autowired
    private TestEntityManager em;

    private User author;
    private User teacher;
    private Topic topic;
    private Competency design;
    private Competency analysis;

    @BeforeEach
    void setUp() {
        author = users.save(new User(null, "author", "An Author", "author@test.co", Role.AUTHOR));
        teacher = users.save(new User(null, "teacher", "A Teacher", "teacher@test.co", Role.TEACHER));
        topic = topics.save(new Topic(null, "Architecture"));
        design = competencies.save(new Competency(null, "DES", "Solution design"));
        analysis = competencies.save(new Competency(null, "ANA", "Problem analysis"));
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private long count(String sql, Object id) {
        return ((Number) em.getEntityManager().createNativeQuery(sql).setParameter(1, id)
                .getSingleResult()).longValue();
    }

    private Question publish(Question question) {
        question.classify(topic.getId(), Set.of(design.getId()));
        question.submitForReview();
        question.startReview();
        question.approve(LocalDateTime.now());
        question.publish();
        return questions.save(question);
    }

    @Test
    void savesAndReadsBackAllRelationships() {
        MultipleChoiceQuestion draft = MultipleChoiceQuestion.draft("Which pattern splits a system into tiers?",
                Difficulty.HIGH, author.getId(), SampleQuestions.fiveOptions(),
                new QuestionMetadata(CognitiveLevel.ANALYZE, 90, "Reviewed by the committee", null));
        draft.classify(topic.getId(), Set.of(design.getId(), analysis.getId()));
        Long id = questions.save(draft).getId();
        flushAndClear();

        MultipleChoiceQuestion loaded = (MultipleChoiceQuestion) questions.findById(id).orElseThrow();
        assertEquals(author.getId(), loaded.getAuthorId());                                  // many-to-one
        assertEquals(topic.getId(), loaded.getTopicId());                                    // many-to-one
        assertEquals(Set.of(design.getId(), analysis.getId()), loaded.getCompetencyIds());   // many-to-many
        assertEquals(5, loaded.getOptions().size());                                        // one-to-many
        assertEquals("Layers", loaded.getCorrectOption().getText());
        assertEquals("Singleton", loaded.getOptions().get(0).getText());                     // order kept
        assertEquals(CognitiveLevel.ANALYZE, loaded.getMetadata().getCognitiveLevel());      // one-to-one
        assertEquals(90, loaded.getMetadata().getEstimatedTimeSeconds());
    }

    @Test
    void inheritanceStoresEachTypeInItsOwnTable() {
        Long mcId = questions.save(SampleQuestions.multipleChoice(author.getId())).getId();
        Long tfId = questions.save(SampleQuestions.trueFalse(author.getId())).getId();
        flushAndClear();

        assertInstanceOf(MultipleChoiceQuestion.class, questions.findById(mcId).orElseThrow());
        TrueFalseQuestion tf = assertInstanceOf(TrueFalseQuestion.class, questions.findById(tfId).orElseThrow());
        assertEquals(false, tf.getCorrectAnswer());
        assertEquals(2, questions.findAll().size());

        assertEquals(1, count("select count(*) from multiple_choice_questions where id = ?1", mcId));
        assertEquals(1, count("select count(*) from true_false_questions where id = ?1", tfId));
        assertEquals(0, count("select count(*) from true_false_questions where id = ?1", mcId));
        assertEquals("TRUE_FALSE", em.getEntityManager()
                .createNativeQuery("select question_type from questions where id = ?1")
                .setParameter(1, tfId).getSingleResult());
    }

    @Test
    void statusChangesArePersistedAndQueryable() {
        Question published = publish(SampleQuestions.trueFalse(author.getId()));
        questions.save(SampleQuestions.multipleChoice(author.getId()));
        flushAndClear();

        assertEquals(QuestionStatus.PUBLISHED, questions.findById(published.getId()).orElseThrow().getStatus());
        assertNotNull(questions.findById(published.getId()).orElseThrow().getMetadata().getLastReviewedAt());
        assertEquals(1, questions.findByStatus(QuestionStatus.DRAFT).size());
        assertEquals(2, questions.findByAuthor(author.getId()).size());
        assertEquals(List.of(published.getId()),
                questions.findPublishedByCompetency(design.getId()).stream().map(Question::getId).toList());
        assertTrue(questions.findPublishedByCompetency(analysis.getId()).isEmpty());
    }

    @Test
    void optionsAreUpdatedInPlace() {
        MultipleChoiceQuestion saved = (MultipleChoiceQuestion) questions.save(
                SampleQuestions.multipleChoice(author.getId()));
        flushAndClear();

        List<AnswerOption> changed = List.of(AnswerOption.of("A", false), AnswerOption.of("B", false),
                AnswerOption.of("C", true), AnswerOption.of("D", false), AnswerOption.of("E", false));
        MultipleChoiceQuestion edited = new MultipleChoiceQuestion(saved.getId(), saved.getStatement(),
                saved.getDifficulty(), saved.getStatus(), saved.getAuthorId(), null, Set.of(),
                saved.getMetadata(), null, changed);
        questions.save(edited);
        flushAndClear();

        MultipleChoiceQuestion loaded = (MultipleChoiceQuestion) questions.findById(saved.getId()).orElseThrow();
        assertEquals("C", loaded.getCorrectOption().getText());
        assertEquals(5, count("select count(*) from answer_options where question_id = ?1", saved.getId()));
    }

    @Test
    void deletingAQuestionRemovesItsOptionsAndMetadata() {
        Long id = questions.save(SampleQuestions.multipleChoice(author.getId())).getId();
        flushAndClear();

        questions.deleteById(id);
        flushAndClear();

        assertTrue(questions.findById(id).isEmpty());
        assertEquals(0, count("select count(*) from answer_options where question_id = ?1", id));
        assertEquals(0, count("select count(*) from question_metadata where question_id = ?1", id));
    }

    @Test
    void repositoryRejectsUnknownAuthor() {
        TrueFalseQuestion orphan = SampleQuestions.trueFalse(999L);
        assertThrows(IllegalArgumentException.class, () -> questions.save(orphan));
    }

    @Test
    void foreignKeyRejectsQuestionWithUnknownAuthor() {
        var insert = em.getEntityManager().createNativeQuery(
                "insert into questions (question_type, statement, difficulty, status, author_id) "
                        + "values ('TRUE_FALSE', 'x', 'LOW', 'DRAFT', 999)");
        assertThrows(PersistenceException.class, insert::executeUpdate);
    }

    @Test
    void foreignKeyRejectsOptionWithoutQuestion() {
        var insert = em.getEntityManager().createNativeQuery(
                "insert into answer_options (text, correct, position, question_id) values ('x', false, 1, 999)");
        assertThrows(PersistenceException.class, insert::executeUpdate);
    }

    @Test
    void primaryKeyPreventsTwoMetadataRowsForOneQuestion() {
        Long id = questions.save(SampleQuestions.trueFalse(author.getId())).getId();
        em.flush();
        var insert = em.getEntityManager().createNativeQuery(
                "insert into question_metadata (question_id, cognitive_level, estimated_time_seconds) "
                        + "values (?1, 'APPLY', 30)").setParameter(1, id);
        assertThrows(PersistenceException.class, insert::executeUpdate);
    }

    @Test
    void questionUsedInAQuizCannotBeDeleted() {
        Question published = publish(SampleQuestions.trueFalse(author.getId()));
        Quiz quiz = new Quiz(null, "Mock exam", teacher.getId(), null, List.of());
        quiz.addQuestion(published, 1.0);
        quizzes.save(quiz);
        flushAndClear();

        questions.deleteById(published.getId());
        assertThrows(PersistenceException.class, em::flush);
    }

    @Test
    void quizKeepsItsQuestionsInOrder() {
        Question first = publish(SampleQuestions.trueFalse(author.getId()));
        Question second = publish(SampleQuestions.multipleChoice(author.getId()));
        Quiz quiz = new Quiz(null, "Mock exam", teacher.getId(), null, List.of());
        quiz.addQuestion(second, 2.0);
        quiz.addQuestion(first, 1.0);
        Long quizId = quizzes.save(quiz).getId();
        flushAndClear();

        Quiz loaded = quizzes.findById(quizId).orElseThrow();
        assertEquals(teacher.getId(), loaded.getTeacherId());
        assertEquals(List.of(second.getId(), first.getId()),
                loaded.getItems().stream().map(i -> i.questionId()).toList());
        assertEquals(3.0, loaded.totalPoints());
    }

    @Test
    void uniqueConstraintPreventsDuplicatedUsernames() {
        users.save(new User(null, "dup", "One", "one@test.co", Role.STUDENT));
        // With IDENTITY keys the INSERT runs on save; Spring translates the error.
        User duplicated = new User(null, "dup", "Two", "two@test.co", Role.STUDENT);
        assertThrows(DataIntegrityViolationException.class, () -> users.save(duplicated));
    }
}
