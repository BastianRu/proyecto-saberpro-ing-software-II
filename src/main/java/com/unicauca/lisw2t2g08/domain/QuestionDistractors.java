package com.unicauca.lisw2t2g08.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Capa de dominio.
 *
 * Objeto de valor que agrupa las opciones de respuesta de una pregunta: la
 * respuesta correcta y los distractores (opciones incorrectas).
 *
 * SRP: su unica responsabilidad es custodiar las opciones y garantizar que sean
 * consistentes (minimo dos opciones y un indice correcto valido).
 */
public class QuestionDistractors {

    private static final char FIRST_LETTER = 'A';

    private final List<String> options;
    private final int correctIndex;

    /**
     * @param options      opciones de respuesta en el orden en que se muestran.
     * @param correctIndex posicion (base 0) de la respuesta correcta.
     * @throws IllegalArgumentException si hay menos de dos opciones, si alguna
     *                                  esta vacia o si el indice esta fuera de rango.
     */
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

    /**
     * @return todas las opciones en orden (inmutable).
     */
    public List<String> getOptions() {
        return options;
    }

    /**
     * @return posicion base 0 de la respuesta correcta.
     */
    public int getCorrectIndex() {
        return correctIndex;
    }

    /**
     * @return texto de la respuesta correcta.
     */
    public String getCorrectAnswer() {
        return options.get(correctIndex);
    }

    /**
     * @return letra con la que se rotula la respuesta correcta (A, B, C, ...).
     */
    public char getCorrectLetter() {
        return letterOf(correctIndex);
    }

    /**
     * @return solo las opciones incorrectas, es decir, los distractores.
     */
    public List<String> getDistractors() {
        List<String> distractors = new ArrayList<>(options);
        distractors.remove(correctIndex);
        return Collections.unmodifiableList(distractors);
    }

    /**
     * @param index posicion base 0 de una opcion.
     * @return letra con la que se rotula esa opcion.
     */
    public static char letterOf(int index) {
        return (char) (FIRST_LETTER + index);
    }

    /**
     * @param index posicion base 0 de una opcion.
     * @return la opcion rotulada, por ejemplo "B. Modelar el dominio del negocio".
     */
    public String getLabeledOption(int index) {
        return letterOf(index) + ". " + options.get(index);
    }
}
