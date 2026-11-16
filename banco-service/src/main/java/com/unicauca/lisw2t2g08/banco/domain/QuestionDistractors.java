package com.unicauca.lisw2t2g08.banco.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Objeto de valor que agrupa las opciones de respuesta de una pregunta:
 * la respuesta correcta y los distractores (opciones incorrectas).
 */
public class QuestionDistractors {

    private static final char FIRST_LETTER = 'A';

    private final List<String> options;
    private final int correctIndex;

    public QuestionDistractors(List<String> options, int correctIndex) {
        if (options == null || options.size() < 2) {
            throw new IllegalArgumentException("Una pregunta requiere al menos dos opciones");
        }
        for (String option : options) {
            if (option == null || option.isBlank()) {
                throw new IllegalArgumentException("Las opciones no pueden estar vacias");
            }
        }
        if (correctIndex < 0 || correctIndex >= options.size()) {
            throw new IllegalArgumentException("El indice de la respuesta correcta esta fuera de rango");
        }
        this.options = List.copyOf(options);
        this.correctIndex = correctIndex;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public String getCorrectAnswer() {
        return options.get(correctIndex);
    }

    public char getCorrectLetter() {
        return letterOf(correctIndex);
    }

    public List<String> getDistractors() {
        List<String> distractors = new ArrayList<>(options);
        distractors.remove(correctIndex);
        return Collections.unmodifiableList(distractors);
    }

    public static char letterOf(int index) {
        return (char) (FIRST_LETTER + index);
    }

    public String getLabeledOption(int index) {
        return letterOf(index) + ". " + options.get(index);
    }
}
