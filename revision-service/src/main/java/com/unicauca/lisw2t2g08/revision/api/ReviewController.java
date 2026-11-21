package com.unicauca.lisw2t2g08.revision.api;

import java.time.Instant;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.unicauca.lisw2t2g08.revision.domain.Review;
import com.unicauca.lisw2t2g08.revision.domain.ReviewDecision;
import com.unicauca.lisw2t2g08.revision.domain.ReviewStatus;
import com.unicauca.lisw2t2g08.revision.security.AuthenticatedUser;
import com.unicauca.lisw2t2g08.revision.security.BearerAuthFilter;
import com.unicauca.lisw2t2g08.revision.service.ReviewService;

/** Punto de entrada REST de Revisión. Solo traduce HTTP <-> ReviewService. */
@RestController
@RequestMapping("/revisiones")
public class ReviewController {

    public record VerdictRequest(ReviewDecision decision, String observations) { }

    public record ReviewResponse(Long id, String questionId, String requestedBy, Instant requestedAt,
                                 ReviewStatus status, String reviewer, Instant startedAt,
                                 ReviewDecision decision, String observations, Instant completedAt) {
        static ReviewResponse of(Review r) {
            return new ReviewResponse(r.getId(), r.getQuestionId(), r.getRequestedBy(), r.getRequestedAt(),
                    r.getStatus(), r.getReviewer(), r.getStartedAt(), r.getDecision(), r.getObservations(),
                    r.getCompletedAt());
        }
    }

    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @GetMapping
    public List<ReviewResponse> list(@RequestParam(required = false) ReviewStatus status) {
        return service.list(status).stream().map(ReviewResponse::of).toList();
    }

    @GetMapping("/{id}")
    public ReviewResponse get(@PathVariable long id) {
        return ReviewResponse.of(service.get(id));
    }

    @GetMapping("/preguntas/{questionId}")
    public List<ReviewResponse> historyOf(@PathVariable String questionId) {
        return service.historyOf(questionId).stream().map(ReviewResponse::of).toList();
    }

    @PostMapping("/{id}/tomar")
    public ReviewResponse take(@PathVariable long id,
                               @RequestAttribute(BearerAuthFilter.USER_ATTRIBUTE) AuthenticatedUser user) {
        return ReviewResponse.of(service.take(id, user));
    }

    @PostMapping("/{id}/dictamen")
    public ReviewResponse submitVerdict(@PathVariable long id, @RequestBody VerdictRequest request,
                                        @RequestAttribute(BearerAuthFilter.USER_ATTRIBUTE) AuthenticatedUser user) {
        return ReviewResponse.of(service.submitVerdict(id, user, request.decision(), request.observations()));
    }
}
