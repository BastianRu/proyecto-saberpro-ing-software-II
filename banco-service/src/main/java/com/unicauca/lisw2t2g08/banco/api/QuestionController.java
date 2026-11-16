package com.unicauca.lisw2t2g08.banco.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.unicauca.lisw2t2g08.banco.domain.AuthToken;
import com.unicauca.lisw2t2g08.banco.domain.DifficultyLevel;
import com.unicauca.lisw2t2g08.banco.domain.Question;
import com.unicauca.lisw2t2g08.banco.domain.QuestionCriteria;
import com.unicauca.lisw2t2g08.banco.domain.QuestionState;
import com.unicauca.lisw2t2g08.banco.domain.Role;
import com.unicauca.lisw2t2g08.banco.security.ForbiddenException;
import com.unicauca.lisw2t2g08.banco.security.TokenVerifier;
import com.unicauca.lisw2t2g08.banco.security.UnauthorizedException;
import com.unicauca.lisw2t2g08.banco.service.IQuestionService;

/**
 * Controlador REST para el banco de preguntas.
 */
@RestController
@RequestMapping("/preguntas")
public class QuestionController {

    private final IQuestionService questionService;
    private final TokenVerifier tokenVerifier;

    public QuestionController(IQuestionService questionService, TokenVerifier tokenVerifier) {
        this.questionService = questionService;
        this.tokenVerifier = tokenVerifier;
    }

    @GetMapping
    public List<QuestionResponse> list(
            @RequestParam(required = false) QuestionState state,
            @RequestParam(required = false) DifficultyLevel difficulty,
            @RequestParam(required = false) String competence,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String subtopic,
            @RequestParam(required = false) String text) {

        boolean hasFilters = (state != null || difficulty != null || competence != null
                || topic != null || subtopic != null || text != null);

        if (!hasFilters) {
            return questionService.listQuestions().stream().map(QuestionResponse::of).toList();
        }

        QuestionCriteria criteria = new QuestionCriteria()
                .withState(state)
                .withDifficulty(difficulty)
                .withCompetence(competence)
                .withTopic(topic)
                .withSubtopic(subtopic)
                .withText(text);

        return questionService.searchQuestions(criteria).stream().map(QuestionResponse::of).toList();
    }

    @GetMapping("/{id}")
    public QuestionResponse findById(@PathVariable String id) {
        return questionService.findById(id)
                .map(QuestionResponse::of)
                .orElseThrow(() -> new ResourceNotFoundException("Pregunta no encontrada: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionResponse create(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody QuestionRequest request) {

        AuthToken token = requireUser(authHeader, Role.ADMINISTRADOR, Role.AUTOR_PREGUNTAS);
        Question question = questionService.createQuestion(request.toDraft(), token.getSubject());
        return QuestionResponse.of(question);
    }

    @PutMapping("/{id}")
    public QuestionResponse update(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody QuestionRequest request) {

        AuthToken token = requireUser(authHeader, Role.ADMINISTRADOR, Role.AUTOR_PREGUNTAS);
        Question question = questionService.updateQuestion(id, request.toDraft(), token.getSubject());
        return QuestionResponse.of(question);
    }

    @PatchMapping("/{id}/estado")
    public QuestionResponse changeState(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ChangeStateRequest request) {

        if (request == null || request.state() == null) {
            throw new IllegalArgumentException("El estado destino es obligatorio");
        }

        AuthToken token = requireUser(authHeader, Role.ADMINISTRADOR, Role.AUTOR_PREGUNTAS, Role.REVISOR);
        questionService.changeState(id, request.state(), token.getSubject());

        return questionService.findById(id)
                .map(QuestionResponse::of)
                .orElseThrow(() -> new ResourceNotFoundException("Pregunta no encontrada tras cambio de estado: " + id));
    }

    @GetMapping("/{id}/historial")
    public List<QuestionStateChangeResponse> stateHistory(@PathVariable String id) {
        return questionService.stateHistory(id).stream()
                .map(QuestionStateChangeResponse::of)
                .toList();
    }

    @GetMapping("/estadisticas")
    public QuestionStatisticsResponse statistics() {
        return QuestionStatisticsResponse.of(questionService.getStatistics());
    }

    private AuthToken requireUser(String authHeader, Role... allowedRoles) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new UnauthorizedException("Se requiere el encabezado Authorization con un token Bearer");
        }

        AuthToken token = tokenVerifier.verify(authHeader)
                .orElseThrow(() -> new UnauthorizedException("Token de autorizacion invalido o expirado"));

        if (allowedRoles.length > 0) {
            boolean authorized = false;
            for (Role role : allowedRoles) {
                if (token.getRole() == role) {
                    authorized = true;
                    break;
                }
            }
            if (!authorized) {
                throw new ForbiddenException("El rol " + token.getRole() + " no tiene permisos para esta operacion");
            }
        }

        return token;
    }
}
