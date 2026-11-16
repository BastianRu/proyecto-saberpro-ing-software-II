package com.unicauca.lisw2t2g08.banco.domain;

import java.util.EnumMap;
import java.util.Map;

/**
 * Conteo y porcentajes de preguntas por cada estado del ciclo de vida.
 */
public class QuestionStatistics {

    private final Map<QuestionState, Integer> counts;
    private final int total;

    public QuestionStatistics(Map<QuestionState, Integer> counts) {
        Map<QuestionState, Integer> copy = new EnumMap<>(QuestionState.class);
        int sum = 0;
        for (QuestionState state : QuestionState.values()) {
            int value = (counts == null) ? 0 : counts.getOrDefault(state, 0);
            copy.put(state, value);
            sum += value;
        }
        this.counts = copy;
        this.total = sum;
    }

    public int getCount(QuestionState state) {
        return counts.getOrDefault(state, 0);
    }

    public int getTotal() {
        return total;
    }

    public double getPercentage(QuestionState state) {
        if (total == 0) {
            return 0.0;
        }
        return (getCount(state) * 100.0) / total;
    }

    public Map<QuestionState, Integer> asMap() {
        return new EnumMap<>(counts);
    }
}
