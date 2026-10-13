package com.unicauca.lisw2t2g08.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuestionCriteriaTest {

    private final Question question = new Question("P-001", "Patron Observer",
            "Contexto sobre notificaciones", "¿Que resuelve Observer?",
            new QuestionDistractors(List.of("Uno", "Dos"), 0),
            "Justificacion", "Bibliografia", "Diseno de software", "Patrones", "Observer",
            DifficultyLevel.MEDIA, QuestionState.BORRADOR);

    @Test
    void sinFiltrosCoincideConTodo() {
        assertTrue(new QuestionCriteria().matches(question));
        assertTrue(new QuestionCriteria().withTopic("  ").withText("").matches(question));
    }

    @Test
    void filtraPorEstadoYDificultad() {
        assertTrue(new QuestionCriteria().withState(QuestionState.BORRADOR)
                .withDifficulty(DifficultyLevel.MEDIA).matches(question));
        assertFalse(new QuestionCriteria().withState(QuestionState.ARCHIVADA).matches(question));
        assertFalse(new QuestionCriteria().withDifficulty(DifficultyLevel.ALTA).matches(question));
    }

    @Test
    void comparaClasificacionSinDistinguirMayusculas() {
        assertTrue(new QuestionCriteria().withCompetence("DISENO DE SOFTWARE")
                .withTopic("patrones").withSubtopic("observer").matches(question));
        assertFalse(new QuestionCriteria().withTopic("Arquitectura").matches(question));
    }

    @Test
    void buscaTextoEnNombreContextoYPregunta() {
        assertTrue(new QuestionCriteria().withText("notificaciones").matches(question));
        assertTrue(new QuestionCriteria().withText("QUE RESUELVE").matches(question));
        assertFalse(new QuestionCriteria().withText("inexistente").matches(question));
    }

    @Test
    void preguntaSinDificultadNoCoincideConUnNivel() {
        Question legacy = new Question("P-002", "Vieja", "Enunciado",
                new QuestionDistractors(List.of("Uno", "Dos"), 0), QuestionState.BORRADOR);
        assertFalse(new QuestionCriteria().withDifficulty(DifficultyLevel.BAJA).matches(legacy));
    }
}
