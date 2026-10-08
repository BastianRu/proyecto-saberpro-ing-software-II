package com.unicauca.lisw2t2g08.domain;

import java.util.EnumMap;
import java.util.Map;

/**
 * Capa de dominio.
 * Objeto de valor inmutable con el conteo de preguntas por estado. Es la
 * informacion que el sujeto publica a sus observadores, de modo que las vistas
 * no tienen que recalcular nada ni conocer el repositorio.
 */
public class QuestionStatistics {

    private final Map<QuestionState, Integer> counts;
    private final int total;

    /**
     * @param counts conteo por estado; los estados ausentes se asumen en cero.
     */
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

    /**
     * @param state estado consultado.
     * @return cantidad de preguntas en ese estado.
     */
    public int getCount(QuestionState state) {
        return counts.getOrDefault(state, 0);
    }

    /**
     * @return total de preguntas contabilizadas.
     */
    public int getTotal() {
        return total;
    }

    /**
     * @param state estado consultado.
     * @return porcentaje de preguntas en ese estado; 0 si no hay preguntas.
     */
    public double getPercentage(QuestionState state) {
        if (total == 0) {
            return 0.0;
        }
        return (getCount(state) * 100.0) / total;
    }

    /**
     * @return copia del conteo por estado.
     */
    public Map<QuestionState, Integer> asMap() {
        return new EnumMap<>(counts);
    }
}
