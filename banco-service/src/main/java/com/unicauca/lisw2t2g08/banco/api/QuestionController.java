package com.unicauca.lisw2t2g08.banco.api;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.unicauca.lisw2t2g08.banco.domain.DifficultyLevel;
import com.unicauca.lisw2t2g08.banco.domain.Question;
import com.unicauca.lisw2t2g08.banco.domain.QuestionCriteria;
import com.unicauca.lisw2t2g08.banco.domain.QuestionDraft;
import com.unicauca.lisw2t2g08.banco.domain.QuestionState;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStates;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStatistics;
import com.unicauca.lisw2t2g08.banco.security.AuthenticatedUser;
import com.unicauca.lisw2t2g08.banco.security.BearerAuthFilter;
import com.unicauca.lisw2t2g08.banco.service.IQuestionService;

/** Punto de entrada REST del Banco. Solo traduce HTTP <-> IQuestionService. */
@RestController
@RequestMapping("/preguntas")
public class QuestionController {

    public record StateChangeRequest(String state) { }

    public record QuestionResponse(String id, String name, String context, String directQuestion,
                                   List<String> options, int correctIndex, String justification,
                                   String bibliography, String competence, String topic,
                                   String subtopic, DifficultyLevel difficulty, String state,
                                   String stateLabel, List<String> allowedTransitions) {
        static QuestionResponse of(Question q) {
            QuestionState state = q.getState();
            return new QuestionResponse(q.getId(), q.getName(), q.getContext(), q.getStatement(),
                    q.getDistractors().getOptions(), q.getDistractors().getCorrectIndex(),
                    q.getJustification(), q.getBibliography(), q.getCompetence(), q.getTopic(),
                    q.getSubtopic(), q.getDifficulty(), state.getCode(), state.getLabel(),
                    state.allowedTransitions().stream().map(QuestionState::getCode).toList());
        }
    }

    public record StatisticsResponse(int total, Map<String, StateCount> byState) {
        public record StateCount(int count, double percentage) { }
    }

    private final IQuestionService service;

    public QuestionController(IQuestionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionResponse create(@RequestBody QuestionDraft draft,
                                   @RequestAttribute(BearerAuthFilter.USER_ATTRIBUTE) AuthenticatedUser user) {
        return QuestionResponse.of(service.createQuestion(draft, user.login()));
    }

    @GetMapping
    public List<QuestionResponse> search(@RequestParam(required = false) String text,
                                         @RequestParam(required = false) String state,
                                         @RequestParam(required = false) String difficulty,
                                         @RequestParam(required = false) String competence,
                                         @RequestParam(required = false) String topic,
                                         @RequestParam(required = false) String subtopic) {
        QuestionCriteria criteria = new QuestionCriteria().withText(text).withCompetence(competence)
                .withTopic(topic).withSubtopic(subtopic);
        if (state != null && !state.isBlank()) {
            criteria.withState(QuestionStates.fromCode(state));
        }
        if (difficulty != null && !difficulty.isBlank()) {
            criteria.withDifficulty(DifficultyLevel.valueOf(difficulty.trim()));
        }
        return service.searchQuestions(criteria).stream().map(QuestionResponse::of).toList();
    }

    @GetMapping("/estadisticas")
    public StatisticsResponse statistics() {
        QuestionStatistics stats = service.getStatistics();
        Map<String, StatisticsResponse.StateCount> byState = new java.util.LinkedHashMap<>();
        for (QuestionState state : QuestionStates.all()) {
            byState.put(state.getCode(),
                    new StatisticsResponse.StateCount(stats.getCount(state), stats.getPercentage(state)));
        }
        return new StatisticsResponse(stats.getTotal(), byState);
    }

    @GetMapping("/{id}")
    public QuestionResponse get(@PathVariable String id) {
        return QuestionResponse.of(service.getQuestion(id));
    }

    @PutMapping("/{id}")
    public QuestionResponse update(@PathVariable String id, @RequestBody QuestionDraft draft) {
        return QuestionResponse.of(service.updateQuestion(id, draft));
    }

    @PutMapping("/{id}/estado")
    public QuestionResponse changeState(@PathVariable String id, @RequestBody StateChangeRequest request,
                                        @RequestAttribute(BearerAuthFilter.USER_ATTRIBUTE) AuthenticatedUser user) {
        return QuestionResponse.of(service.changeState(id, request.state(), user.login()));
    }
}
