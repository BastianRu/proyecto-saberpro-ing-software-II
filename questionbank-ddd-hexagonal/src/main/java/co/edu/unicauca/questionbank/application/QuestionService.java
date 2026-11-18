package co.edu.unicauca.questionbank.application;

import co.edu.unicauca.questionbank.domain.exception.BusinessRuleException;
import co.edu.unicauca.questionbank.domain.model.AnswerOption;
import co.edu.unicauca.questionbank.domain.model.Difficulty;
import co.edu.unicauca.questionbank.domain.model.MultipleChoiceQuestion;
import co.edu.unicauca.questionbank.domain.model.Question;
import co.edu.unicauca.questionbank.domain.model.QuestionMetadata;
import co.edu.unicauca.questionbank.domain.model.QuestionStatus;
import co.edu.unicauca.questionbank.domain.model.Role;
import co.edu.unicauca.questionbank.domain.model.TrueFalseQuestion;
import co.edu.unicauca.questionbank.domain.model.User;
import co.edu.unicauca.questionbank.domain.repository.CompetencyRepository;
import co.edu.unicauca.questionbank.domain.repository.QuestionRepository;
import co.edu.unicauca.questionbank.domain.repository.TopicRepository;
import co.edu.unicauca.questionbank.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Use cases of the author: create, query, classify, submit for review,
 * correct after a rejection, publish and archive questions.
 *
 * <p>The service checks the references (author, topic, competencies) and lets
 * the domain enforce the lifecycle rules; then it persists through the
 * {@link QuestionRepository} contract inside a transaction.</p>
 */
@Service
@Transactional
public class QuestionService {

    private final QuestionRepository questions;
    private final UserRepository users;
    private final TopicRepository topics;
    private final CompetencyRepository competencies;

    /**
     * Creates the service.
     *
     * @param questions    question repository contract
     * @param users        user repository contract
     * @param topics       topic repository contract
     * @param competencies competency repository contract
     */
    public QuestionService(QuestionRepository questions, UserRepository users,
                           TopicRepository topics, CompetencyRepository competencies) {
        this.questions = questions;
        this.users = users;
        this.topics = topics;
        this.competencies = competencies;
    }

    /**
     * Use case 1: creates a multiple choice question in DRAFT.
     *
     * @param statement     statement
     * @param difficulty    difficulty
     * @param authorId      author (must have the AUTHOR role)
     * @param options       four distractors and one correct answer
     * @param metadata      metadata, or null for defaults
     * @param topicId       optional topic
     * @param competencyIds optional competencies (required if a topic is given)
     * @return the stored question
     */
    public Question createMultipleChoice(String statement, Difficulty difficulty, Long authorId,
                                         List<AnswerOption> options, QuestionMetadata metadata,
                                         Long topicId, Set<Long> competencyIds) {
        requireAuthor(authorId);
        Question question = MultipleChoiceQuestion.draft(statement, difficulty, authorId, options, metadata);
        return store(question, topicId, competencyIds);
    }

    /**
     * Use case 1: creates a true/false question in DRAFT.
     *
     * @param statement     statement
     * @param difficulty    difficulty
     * @param authorId      author (must have the AUTHOR role)
     * @param correctAnswer expected truth value
     * @param metadata      metadata, or null for defaults
     * @param topicId       optional topic
     * @param competencyIds optional competencies (required if a topic is given)
     * @return the stored question
     */
    public Question createTrueFalse(String statement, Difficulty difficulty, Long authorId,
                                    boolean correctAnswer, QuestionMetadata metadata,
                                    Long topicId, Set<Long> competencyIds) {
        requireAuthor(authorId);
        Question question = TrueFalseQuestion.draft(statement, difficulty, authorId, correctAnswer, metadata);
        return store(question, topicId, competencyIds);
    }

    private Question store(Question question, Long topicId, Set<Long> competencyIds) {
        if (topicId != null || (competencyIds != null && !competencyIds.isEmpty())) {
            requireClassification(topicId, competencyIds);
            question.classify(topicId, competencyIds);
        }
        return questions.save(question);
    }

    /**
     * Use case 2: finds a question.
     *
     * @param id question identifier
     * @return the question
     * @throws ResourceNotFoundException if it does not exist
     */
    @Transactional(readOnly = true)
    public Question findById(Long id) {
        return questions.findById(id).orElseThrow(() -> new ResourceNotFoundException("Question", id));
    }

    /** @return every question */
    @Transactional(readOnly = true)
    public List<Question> findAll() {
        return questions.findAll();
    }

    /**
     * @param authorId author identifier
     * @return questions of the author
     */
    @Transactional(readOnly = true)
    public List<Question> findByAuthor(Long authorId) {
        return questions.findByAuthor(authorId);
    }

    /**
     * @param status lifecycle status
     * @return questions in that status
     */
    @Transactional(readOnly = true)
    public List<Question> findByStatus(QuestionStatus status) {
        return questions.findByStatus(status);
    }

    /**
     * @param competencyId competency identifier
     * @return published questions assessing the competency
     */
    @Transactional(readOnly = true)
    public List<Question> findPublishedByCompetency(Long competencyId) {
        return questions.findPublishedByCompetency(competencyId);
    }

    /**
     * Use case 3: associates a question with a topic and one or more competencies.
     *
     * @param questionId    question identifier
     * @param topicId       topic identifier
     * @param competencyIds competency identifiers
     * @return the updated question
     */
    public Question classify(Long questionId, Long topicId, Set<Long> competencyIds) {
        Question question = findById(questionId);
        requireClassification(topicId, competencyIds);
        question.classify(topicId, competencyIds);
        return questions.save(question);
    }

    /**
     * Use case 4: sends a draft to review (DRAFT -> PENDING_REVIEW) after
     * checking its minimum conditions.
     *
     * @param questionId question identifier
     * @return the updated question
     */
    public Question submitForReview(Long questionId) {
        Question question = findById(questionId);
        question.submitForReview();
        return questions.save(question);
    }

    /**
     * REJECTED -> DRAFT, so the author can correct and resubmit the question.
     *
     * @param questionId question identifier
     * @return the updated question
     */
    public Question reopenForCorrection(Long questionId) {
        Question question = findById(questionId);
        question.reopenForCorrection();
        return questions.save(question);
    }

    /**
     * APPROVED -> PUBLISHED.
     *
     * @param questionId question identifier
     * @return the updated question
     */
    public Question publish(Long questionId) {
        Question question = findById(questionId);
        question.publish();
        return questions.save(question);
    }

    /**
     * PUBLISHED -> ARCHIVED.
     *
     * @param questionId question identifier
     * @return the updated question
     */
    public Question archive(Long questionId) {
        Question question = findById(questionId);
        question.archive();
        return questions.save(question);
    }

    /**
     * Deletes a question that is still a draft; its options and metadata are
     * removed with it.
     *
     * @param questionId question identifier
     */
    public void deleteDraft(Long questionId) {
        Question question = findById(questionId);
        if (question.getStatus() != QuestionStatus.DRAFT) {
            throw new BusinessRuleException("Only DRAFT questions can be deleted, current status: "
                    + question.getStatus());
        }
        questions.deleteById(questionId);
    }

    private void requireAuthor(Long authorId) {
        User author = users.findById(authorId).orElseThrow(() -> new ResourceNotFoundException("User", authorId));
        if (!author.hasRole(Role.AUTHOR)) {
            throw new BusinessRuleException("User " + authorId + " is not an AUTHOR");
        }
    }

    private void requireClassification(Long topicId, Set<Long> competencyIds) {
        if (topicId != null && topics.findById(topicId).isEmpty()) {
            throw new ResourceNotFoundException("Topic", topicId);
        }
        if (competencyIds != null) {
            for (Long competencyId : competencyIds) {
                if (competencies.findById(competencyId).isEmpty()) {
                    throw new ResourceNotFoundException("Competency", competencyId);
                }
            }
        }
    }
}
