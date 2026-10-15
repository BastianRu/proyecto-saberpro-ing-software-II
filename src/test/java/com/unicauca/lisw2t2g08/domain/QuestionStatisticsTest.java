package com.unicauca.lisw2t2g08.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias del objeto de valor QuestionStatistics.
 */
class QuestionStatisticsTest {

    private QuestionStatistics statistics(int borrador, int pendiente, int archivada) {
        Map<QuestionState, Integer> counts = new EnumMap<>(QuestionState.class);
        counts.put(QuestionState.BORRADOR, borrador);
        counts.put(QuestionState.PENDIENTE_REVISION, pendiente);
        counts.put(QuestionState.ARCHIVADA, archivada);
        return new QuestionStatistics(counts);
    }

    @Test
    @DisplayName("Suma el total a partir del conteo por estado")
    void shouldComputeTotal() {
        assertEquals(50, statistics(25, 18, 7).getTotal());
    }

    @Test
    @DisplayName("Calcula el porcentaje de cada estado")
    void shouldComputePercentages() {
        QuestionStatistics stats = statistics(5, 3, 2);

        assertEquals(50.0, stats.getPercentage(QuestionState.BORRADOR), 0.0001);
        assertEquals(30.0, stats.getPercentage(QuestionState.PENDIENTE_REVISION), 0.0001);
        assertEquals(20.0, stats.getPercentage(QuestionState.ARCHIVADA), 0.0001);
    }

    @Test
    @DisplayName("Sin preguntas los porcentajes son cero y no hay division por cero")
    void shouldHandleEmptyStatistics() {
        QuestionStatistics stats = new QuestionStatistics(null);

        assertEquals(0, stats.getTotal());
        for (QuestionState state : QuestionState.values()) {
            assertEquals(0, stats.getCount(state));
            assertEquals(0.0, stats.getPercentage(state), 0.0001);
        }
    }

    @Test
    @DisplayName("Los estados ausentes en el mapa se cuentan como cero")
    void shouldFillMissingStatesWithZero() {
        Map<QuestionState, Integer> counts = new EnumMap<>(QuestionState.class);
        counts.put(QuestionState.BORRADOR, 4);

        QuestionStatistics stats = new QuestionStatistics(counts);

        assertEquals(4, stats.getTotal());
        assertEquals(0, stats.getCount(QuestionState.ARCHIVADA));
        assertEquals(QuestionState.values().length, stats.asMap().size());
    }

    @Test
    @DisplayName("El mapa devuelto es una copia")
    void shouldReturnDefensiveCopy() {
        QuestionStatistics stats = statistics(1, 1, 1);

        stats.asMap().put(QuestionState.BORRADOR, 99);

        assertEquals(1, stats.getCount(QuestionState.BORRADOR));
    }
}
