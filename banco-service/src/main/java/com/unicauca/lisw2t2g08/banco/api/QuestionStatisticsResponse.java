package com.unicauca.lisw2t2g08.banco.api;

import java.util.EnumMap;
import java.util.Map;

import com.unicauca.lisw2t2g08.banco.domain.QuestionState;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStatistics;

/**
 * Conteo y porcentajes de preguntas por estado.
 */
public record QuestionStatisticsResponse(
        Map<QuestionState, Integer> counts,
        Map<QuestionState, Double> percentages,
        int total) {

    public static QuestionStatisticsResponse of(QuestionStatistics stats) {
        Map<QuestionState, Double> percentages = new EnumMap<>(QuestionState.class);
        for (QuestionState state : QuestionState.values()) {
            percentages.put(state, stats.getPercentage(state));
        }
        return new QuestionStatisticsResponse(stats.asMap(), percentages, stats.getTotal());
    }
}
