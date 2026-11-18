package co.edu.unicauca.questionbank.api;

import co.edu.unicauca.questionbank.api.dto.ClassifyRequest;
import co.edu.unicauca.questionbank.api.dto.CreateQuestionRequest;
import co.edu.unicauca.questionbank.api.dto.QuestionResponse;
import co.edu.unicauca.questionbank.api.dto.ReviewRequest;
import co.edu.unicauca.questionbank.application.QuestionService;
import co.edu.unicauca.questionbank.application.ReviewService;
import co.edu.unicauca.questionbank.domain.exception.BusinessRuleException;
import co.edu.unicauca.questionbank.domain.model.AnswerOption;
import co.edu.unicauca.questionbank.domain.model.CognitiveLevel;
import co.edu.unicauca.questionbank.domain.model.Question;
import co.edu.unicauca.questionbank.domain.model.QuestionMetadata;
import co.edu.unicauca.questionbank.domain.model.QuestionStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for questions: creation, queries, classification and the
 * lifecycle transitions. It only translates HTTP to use cases.
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questions;
    private final ReviewService reviews;

    /**
     * Creates the controller.
     *
     * @param questions question use cases
     * @param reviews   review use cases
     */
    public QuestionController(QuestionService questions, ReviewService reviews) {
        this.questions = questions;
        this.reviews = reviews;
    }

    /**
     * Creates a question in DRAFT.
     *
     * @param request question data
     * @return the created question
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionResponse create(@RequestBody CreateQuestionRequest request) {
        if (request.type() == null) {
            throw new BusinessRuleException("The question type is required");
        }
        QuestionMetadata metadata = toMetadata(request.metadata());
        Question created = switch (request.type()) {
            case MULTIPLE_CHOICE -> questions.createMultipleChoice(request.statement(), request.difficulty(),
                    request.authorId(), toOptions(request.options()), metadata,
                    request.topicId(), request.competencyIds());
            case TRUE_FALSE -> {
                if (request.correctAnswer() == null) {
                    throw new BusinessRuleException("A true/false question needs its correct answer");
                }
                yield questions.createTrueFalse(request.statement(), request.difficulty(), request.authorId(),
                        request.correctAnswer(), metadata, request.topicId(), request.competencyIds());
            }
        };
        return QuestionResponse.from(created);
    }

    /**
     * Lists questions. At most one filter is applied, in this order of precedence:
     * competency (published only), status, author.
     *
     * @param competencyId optional competency filter
     * @param status       optional status filter
     * @param authorId     optional author filter
     * @return matching questions
     */
    @GetMapping
    public List<QuestionResponse> list(@RequestParam(required = false) Long competencyId,
                                       @RequestParam(required = false) QuestionStatus status,
                                       @RequestParam(required = false) Long authorId) {
        List<Question> result;
        if (competencyId != null) {
            result = questions.findPublishedByCompetency(competencyId);
        } else if (status != null) {
            result = questions.findByStatus(status);
        } else if (authorId != null) {
            result = questions.findByAuthor(authorId);
        } else {
            result = questions.findAll();
        }
        return result.stream().map(QuestionResponse::from).toList();
    }

    /**
     * @param id question identifier
     * @return the question
     */
    @GetMapping("/{id}")
    public QuestionResponse get(@PathVariable Long id) {
        return QuestionResponse.from(questions.findById(id));
    }

    /**
     * Classifies a draft question.
     *
     * @param id      question identifier
     * @param request topic and competencies
     * @return the updated question
     */
    @PutMapping("/{id}/classification")
    public QuestionResponse classify(@PathVariable Long id, @RequestBody ClassifyRequest request) {
        return QuestionResponse.from(questions.classify(id, request.topicId(), request.competencyIds()));
    }

    /**
     * DRAFT -> PENDING_REVIEW.
     *
     * @param id question identifier
     * @return the updated question
     */
    @PostMapping("/{id}/submit")
    public QuestionResponse submit(@PathVariable Long id) {
        return QuestionResponse.from(questions.submitForReview(id));
    }

    /**
     * PENDING_REVIEW -> IN_REVIEW.
     *
     * @param id      question identifier
     * @param request reviewer
     * @return the updated question
     */
    @PostMapping("/{id}/start-review")
    public QuestionResponse startReview(@PathVariable Long id, @RequestBody ReviewRequest request) {
        return QuestionResponse.from(reviews.startReview(id, request.reviewerId()));
    }

    /**
     * IN_REVIEW -> APPROVED.
     *
     * @param id      question identifier
     * @param request reviewer
     * @return the updated question
     */
    @PostMapping("/{id}/approve")
    public QuestionResponse approve(@PathVariable Long id, @RequestBody ReviewRequest request) {
        return QuestionResponse.from(reviews.approve(id, request.reviewerId()));
    }

    /**
     * IN_REVIEW -> REJECTED.
     *
     * @param id      question identifier
     * @param request reviewer and reason
     * @return the updated question
     */
    @PostMapping("/{id}/reject")
    public QuestionResponse reject(@PathVariable Long id, @RequestBody ReviewRequest request) {
        return QuestionResponse.from(reviews.reject(id, request.reviewerId(), request.reason()));
    }

    /**
     * REJECTED -> DRAFT.
     *
     * @param id question identifier
     * @return the updated question
     */
    @PostMapping("/{id}/reopen")
    public QuestionResponse reopen(@PathVariable Long id) {
        return QuestionResponse.from(questions.reopenForCorrection(id));
    }

    /**
     * APPROVED -> PUBLISHED.
     *
     * @param id question identifier
     * @return the updated question
     */
    @PostMapping("/{id}/publish")
    public QuestionResponse publish(@PathVariable Long id) {
        return QuestionResponse.from(questions.publish(id));
    }

    /**
     * PUBLISHED -> ARCHIVED.
     *
     * @param id question identifier
     * @return the updated question
     */
    @PostMapping("/{id}/archive")
    public QuestionResponse archive(@PathVariable Long id) {
        return QuestionResponse.from(questions.archive(id));
    }

    /**
     * Deletes a draft question with its options and metadata.
     *
     * @param id question identifier
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        questions.deleteDraft(id);
    }

    private static List<AnswerOption> toOptions(List<CreateQuestionRequest.Option> options) {
        if (options == null) {
            return List.of();
        }
        return options.stream().map(o -> AnswerOption.of(o.text(), o.correct())).toList();
    }

    private static QuestionMetadata toMetadata(CreateQuestionRequest.Metadata metadata) {
        if (metadata == null) {
            return null;
        }
        CognitiveLevel level = metadata.cognitiveLevel() == null
                ? CognitiveLevel.UNDERSTAND : metadata.cognitiveLevel();
        int seconds = metadata.estimatedTimeSeconds() == null ? 120 : metadata.estimatedTimeSeconds();
        return new QuestionMetadata(level, seconds, metadata.editorialNotes(), null);
    }
}
