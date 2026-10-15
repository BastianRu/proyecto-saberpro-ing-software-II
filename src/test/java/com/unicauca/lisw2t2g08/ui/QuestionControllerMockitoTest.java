package com.unicauca.lisw2t2g08.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.unicauca.lisw2t2g08.domain.Question;
import com.unicauca.lisw2t2g08.domain.QuestionCriteria;
import com.unicauca.lisw2t2g08.domain.QuestionDraft;
import com.unicauca.lisw2t2g08.domain.QuestionDistractors;
import com.unicauca.lisw2t2g08.domain.QuestionState;
import com.unicauca.lisw2t2g08.domain.QuestionStateChange;
import com.unicauca.lisw2t2g08.domain.QuestionStatistics;
import com.unicauca.lisw2t2g08.service.IQuestionService;

/**
 * Pruebas del controlador del MVC con Mockito.
 *
 * A diferencia de {@code QuestionControllerTest}, que usa un servicio falso
 * escrito a mano, aqui Mockito genera el doble de IQuestionService a partir de
 * las anotaciones {@code @Mock}. El controlador depende de la abstraccion
 * (DIP), de modo que puede aislarse por completo del dominio y de la interfaz
 * grafica y verificarse unicamente la delegacion.
 */
class QuestionControllerMockitoTest {

    /** Doble generado por Mockito a partir de la abstraccion. */
    @Mock
    private IQuestionService service;

    private QuestionController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new QuestionController(service);
    }

    @Test
    @DisplayName("Mockito: el controlador delega listar y buscar en el servicio")
    void shouldDelegateListingAndFinding() {
        Question question = question();
        when(service.listQuestions()).thenReturn(List.of(question));
        when(service.findById("P-001")).thenReturn(Optional.of(question));

        assertEquals(1, controller.listQuestions().size());
        assertSame(question, controller.findById("P-001").orElseThrow());

        verify(service).listQuestions();
        verify(service).findById("P-001");
    }

    @Test
    @DisplayName("Mockito: delega crear, modificar y consultar en el servicio")
    void shouldDelegateCreateUpdateAndSearch() {
        Question question = question();
        QuestionDraft draft = new QuestionDraft();
        QuestionCriteria criteria = new QuestionCriteria();
        when(service.createQuestion(draft)).thenReturn(question);
        when(service.updateQuestion("P-001", draft)).thenReturn(question);
        when(service.searchQuestions(criteria)).thenReturn(List.of(question));

        assertSame(question, controller.createQuestion(draft));
        assertSame(question, controller.updateQuestion("P-001", draft));
        assertEquals(1, controller.searchQuestions(criteria).size());

        verify(service).createQuestion(draft);
        verify(service).updateQuestion("P-001", draft);
        verify(service).searchQuestions(criteria);
    }

    @Test
    @DisplayName("Mockito: delega el cambio de estado con el usuario")
    void shouldDelegateChangeStateWithUser() {
        when(service.changeState("P-001", QuestionState.PENDIENTE_REVISION, "juan"))
                .thenReturn(true);

        assertTrue(controller.changeState("P-001", QuestionState.PENDIENTE_REVISION, "juan"));

        verify(service).changeState("P-001", QuestionState.PENDIENTE_REVISION, "juan");
    }

    @Test
    @DisplayName("Mockito: el cambio sin usuario usa la sobrecarga de dos argumentos")
    void shouldDelegateChangeStateWithoutUser() {
        when(service.changeState("P-001", QuestionState.PENDIENTE_REVISION)).thenReturn(true);

        assertTrue(controller.changeState("P-001", QuestionState.PENDIENTE_REVISION));

        verify(service).changeState("P-001", QuestionState.PENDIENTE_REVISION);
        verify(service, never()).changeState(any(), any(), any());
    }

    @Test
    @DisplayName("Mockito: delega el historial de estados y las estadisticas")
    void shouldDelegateStateHistoryAndStatistics() {
        Map<QuestionState, Integer> counts = new EnumMap<>(QuestionState.class);
        counts.put(QuestionState.BORRADOR, 3);
        when(service.getStatistics()).thenReturn(new QuestionStatistics(counts));
        when(service.stateHistory("P-001")).thenReturn(List.of(change()));

        assertEquals(3, controller.getStatistics().getCount(QuestionState.BORRADOR));
        assertEquals(1, controller.stateHistory("P-001").size());

        verify(service).getStatistics();
        verify(service).stateHistory("P-001");
    }

    private Question question() {
        return new Question("P-001", "Pregunta sobre Mockito", "Enunciado",
                new QuestionDistractors(List.of("Opcion A", "Opcion B"), 1),
                QuestionState.BORRADOR);
    }

    private QuestionStateChange change() {
        return new QuestionStateChange("P-001", QuestionState.BORRADOR,
                QuestionState.PENDIENTE_REVISION, "juan", java.time.LocalDateTime.now());
    }
}
