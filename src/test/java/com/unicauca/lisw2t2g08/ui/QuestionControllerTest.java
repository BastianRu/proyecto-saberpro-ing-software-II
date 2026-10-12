package com.unicauca.lisw2t2g08.ui;

import com.unicauca.lisw2t2g08.service.IQuestionService;
import com.unicauca.lisw2t2g08.domain.Question;
import com.unicauca.lisw2t2g08.domain.QuestionCriteria;
import com.unicauca.lisw2t2g08.domain.QuestionDraft;
import com.unicauca.lisw2t2g08.domain.QuestionDistractors;
import com.unicauca.lisw2t2g08.domain.QuestionState;
import com.unicauca.lisw2t2g08.domain.QuestionStatistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del controlador del MVC.
 *
 * Como el controlador depende de la abstraccion IQuestionService (DIP), se
 * puede probar con un doble de prueba, sin dominio real ni interfaz grafica.
 */
class QuestionControllerTest {

    /** Doble de prueba del servicio de dominio. */
    private static class FakeQuestionService implements IQuestionService {

        private String lastId;
        private QuestionState lastState;

        @Override
        public List<Question> listQuestions() {
            return List.of(question());
        }

        @Override
        public Optional<Question> findById(String id) {
            return "P-001".equals(id) ? Optional.of(question()) : Optional.empty();
        }

        @Override
        public boolean registerQuestion(Question question) {
            return true;
        }

        @Override
        public Question createQuestion(QuestionDraft draft) {
            return question();
        }

        @Override
        public Question updateQuestion(String id, QuestionDraft draft) {
            this.lastId = id;
            return question();
        }

        @Override
        public List<Question> searchQuestions(QuestionCriteria criteria) {
            return List.of(question());
        }

        @Override
        public boolean changeState(String id, QuestionState newState) {
            this.lastId = id;
            this.lastState = newState;
            return true;
        }

        @Override
        public QuestionStatistics getStatistics() {
            Map<QuestionState, Integer> counts = new EnumMap<>(QuestionState.class);
            counts.put(QuestionState.BORRADOR, 3);
            return new QuestionStatistics(counts);
        }

        private Question question() {
            return new Question("P-001", "Pregunta sobre DDD", "Enunciado",
                    new QuestionDistractors(List.of("Opcion A", "Opcion B"), 1),
                    QuestionState.BORRADOR);
        }
    }

    private FakeQuestionService service;
    private QuestionController controller;

    @BeforeEach
    void setUp() {
        service = new FakeQuestionService();
        controller = new QuestionController(service);
    }

    @Test
    @DisplayName("El servicio es obligatorio")
    void shouldRejectNullService() {
        assertThrows(IllegalArgumentException.class, () -> new QuestionController(null));
    }

    @Test
    @DisplayName("Delega el listado de preguntas en el servicio")
    void shouldDelegateListQuestions() {
        assertEquals(1, controller.listQuestions().size());
    }

    @Test
    @DisplayName("Delega la busqueda por id en el servicio")
    void shouldDelegateFindById() {
        assertTrue(controller.findById("P-001").isPresent());
        assertTrue(controller.findById("P-999").isEmpty());
    }

    @Test
    @DisplayName("Traslada al servicio el cambio de estado solicitado por la vista")
    void shouldDelegateChangeState() {
        assertTrue(controller.changeState("P-001", QuestionState.ELIMINADA));

        assertEquals("P-001", service.lastId);
        assertEquals(QuestionState.ELIMINADA, service.lastState);
    }

    @Test
    @DisplayName("Delega el calculo de estadisticas en el servicio")
    void shouldDelegateStatistics() {
        assertEquals(3, controller.getStatistics().getCount(QuestionState.BORRADOR));
    }

    @Test
    @DisplayName("Delega crear, modificar y consultar en el servicio")
    void shouldDelegateCreateUpdateAndSearch() {
        assertEquals("P-001", controller.createQuestion(new QuestionDraft()).getId());
        controller.updateQuestion("P-001", new QuestionDraft());
        assertEquals("P-001", service.lastId);
        assertEquals(1, controller.searchQuestions(new QuestionCriteria()).size());
    }
}
