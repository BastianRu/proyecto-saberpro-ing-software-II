package com.unicauca.lisw2t2g08.banco.domain;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Objeto de valor inmutable con el conteo de preguntas por estado. Se indexa
 * por código de estado para no atar al cliente a las clases del dominio.
 */
public class QuestionStatistics {

    private final Map<String, Integer> counts;
    private final int total;

    /** @param counts conteo por código de estado; los ausentes se asumen en cero. */
    public QuestionStatistics(Map<String, Integer> counts) {
        Map<String, Integer> copy = new LinkedHashMap<>();
        int sum = 0;
        for (QuestionState state : QuestionStates.all()) {
            int value = (counts == null) ? 0 : counts.getOrDefault(state.getCode(), 0);
            copy.put(state.getCode(), value);
            sum += value;
        }
        this.counts = copy;
        this.total = sum;
    }

    public int getCount(QuestionState state) {
        return counts.getOrDefault(state.getCode(), 0);
    }

    public int getTotal() {
        return total;
    }

    /** @return porcentaje de preguntas en ese estado; 0 si no hay preguntas. */
    public double getPercentage(QuestionState state) {
        return total == 0 ? 0.0 : (getCount(state) * 100.0) / total;
    }

    /** @return copia del conteo por código de estado, en el orden del ciclo de vida. */
    public Map<String, Integer> asMap() {
        return new LinkedHashMap<>(counts);
    }
}
