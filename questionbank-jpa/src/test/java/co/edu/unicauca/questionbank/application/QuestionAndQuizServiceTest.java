package co.edu.unicauca.questionbank.application;

import co.edu.unicauca.questionbank.application.fake.InMemoryRepositories;
import co.edu.unicauca.questionbank.domain.SampleQuestions;
import co.edu.unicauca.questionbank.domain.exception.BusinessRuleException;
import co.edu.unicauca.questionbank.domain.model.Difficulty;
import co.edu.unicauca.questionbank.domain.model.Question;
import co.edu.unicauca.questionbank.domain.model.QuestionStatus;
import co.edu.unicauca.questionbank.domain.model.Quiz;
import co.edu.unicauca.questionbank.domain.model.Role;
import co.edu.unicauca.questionbank.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the use cases against in-memory repositories: the application layer
 * depends only on the repository contracts, so no database is needed.
 */
class QuestionAndQuizServiceTest {

    private QuestionService questionService;
    private ReviewService reviewService;
    private QuizService quizService;
    private InMemoryRepositories.Questions questions;
    private User author;
    private User reviewer;
    private User teacher;
    private Long topicId;
    private Long competencyId;

    @BeforeEach
    void setUp() {
        InMemoryRepositories.Users users = new InMemoryRepositories.Users();
        InMemoryRepositories.Topics topics = new InMemoryRepositories.Topics();
        InMemoryRepositories.Competencies competencies = new InMemoryRepositories.Competencies();
        questions = new InMemoryRepositories.Questions();
        CatalogService catalog = new CatalogService(users, topics, competencies);
        questionService = new QuestionService(questions, users, topics, competencies);
        reviewService = new ReviewService(questions, users);
        quizService = new QuizService(new InMemoryRepositories.Quizzes(), questions, users);

        author = catalog.registerUser("author", "An Author", "author@test.co", Role.AUTHOR);
        reviewer = catalog.registerUser("reviewer", "A Reviewer", "reviewer@test.co", Role.REVIEWER);
        teacher = catalog.registerUser("teacher", "A Teacher", "teacher@test.co", Role.TEACHER);
        topicId = catalog.createTopic("Architecture").getId();
        competencyId = catalog.createCompetency("DES", "Solution design").getId();
    }

    private Question createClassified() {
        return questionService.createMultipleChoice("Which pattern splits a system into tiers?", Difficulty.LOW,
                author.getId(), SampleQuestions.fiveOptions(), null, topicId, Set.of(competencyId));
    }

    private Question createPublished() {
        Question question = createClassified();
        questionService.submitForReview(question.getId());
        reviewService.startReview(question.getId(), reviewer.getId());
        reviewService.approve(question.getId(), reviewer.getId());
        return questionService.publish(question.getId());
    }

    @Test
    void createsAndQueriesQuestions() {
        createClassified();
        questionService.createTrueFalse("Statement", Difficulty.LOW, author.getId(), true, null, null, null);
        assertEquals(2, questionService.findByAuthor(author.getId()).size());
        assertEquals(2, questionService.findByStatus(QuestionStatus.DRAFT).size());
    }

    @Test
    void onlyAuthorsCanCreateQuestions() {
        assertThrows(BusinessRuleException.class, () -> questionService.createTrueFalse("Statement",
                Difficulty.LOW, teacher.getId(), true, null, null, null));
    }

    @Test
    void unknownTopicIsRejected() {
        assertThrows(ResourceNotFoundException.class, () -> questionService.createMultipleChoice("Statement?",
                Difficulty.LOW, author.getId(), SampleQuestions.fiveOptions(), null, 99L, Set.of(competencyId)));
    }

    @Test
    void submitForReviewMovesDraftToPendingReview() {
        Question question = createClassified();
        assertEquals(QuestionStatus.PENDING_REVIEW, questionService.submitForReview(question.getId()).getStatus());
    }

    @Test
    void onlyReviewersCanReview() {
        Question question = createClassified();
        questionService.submitForReview(question.getId());
        assertThrows(BusinessRuleException.class, () -> reviewService.startReview(question.getId(), author.getId()));
        assertEquals(QuestionStatus.PENDING_REVIEW, questionService.findById(question.getId()).getStatus());
    }

    @Test
    void publishedQuestionsAreFoundByCompetency() {
        createClassified();
        Question published = createPublished();
        List<Question> result = questionService.findPublishedByCompetency(competencyId);
        assertEquals(List.of(published.getId()), result.stream().map(Question::getId).toList());
    }

    @Test
    void quizAcceptsOnlyPublishedQuestions() {
        Question published = createPublished();
        Quiz quiz = quizService.createQuiz("Mock exam", teacher.getId(), List.of(published.getId()));
        assertEquals(1, quiz.getItems().size());

        Question draft = createClassified();
        assertThrows(BusinessRuleException.class,
                () -> quizService.createQuiz("Bad", teacher.getId(), List.of(published.getId(), draft.getId())));
    }

    @Test
    void onlyDraftsCanBeDeleted() {
        Question published = createPublished();
        assertThrows(BusinessRuleException.class, () -> questionService.deleteDraft(published.getId()));
        Question draft = createClassified();
        questionService.deleteDraft(draft.getId());
        assertEquals(1, questions.findAll().size());
    }
}
