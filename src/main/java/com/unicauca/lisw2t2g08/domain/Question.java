package com.unicauca.lisw2t2g08.domain;

import java.util.Objects;

/**
 * Capa de dominio.
 *
 * Entidad principal del banco de preguntas. Encapsula sus reglas de negocio
 * basicas: identidad obligatoria, datos no vacios y transiciones de estado
 * validas.
 */
public class Question {

    private final String id;
    private String name;
    private String statement;
    private QuestionDistractors distractors;
    private QuestionState state;

    /**
     * @param id          identificador unico, por ejemplo "P-001".
     * @param name        nombre corto de la pregunta.
     * @param statement   enunciado completo.
     * @param distractors opciones de respuesta con su respuesta correcta.
     * @param state       estado inicial; si es null se asume BORRADOR.
     */
    public Question(String id, String name, String statement,
                    QuestionDistractors distractors, QuestionState state) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id de la pregunta es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre de la pregunta es obligatorio");
        }
        if (statement == null || statement.isBlank()) {
            throw new IllegalArgumentException("El enunciado de la pregunta es obligatorio");
        }
        if (distractors == null) {
            throw new IllegalArgumentException("La pregunta requiere opciones de respuesta");
        }
        this.id = id;
        this.name = name;
        this.statement = statement;
        this.distractors = distractors;
        this.state = (state == null) ? QuestionState.BORRADOR : state;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre de la pregunta es obligatorio");
        }
        this.name = name;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        if (statement == null || statement.isBlank()) {
            throw new IllegalArgumentException("El enunciado de la pregunta es obligatorio");
        }
        this.statement = statement;
    }

    public QuestionDistractors getDistractors() {
        return distractors;
    }

    public void setDistractors(QuestionDistractors distractors) {
        if (distractors == null) {
            throw new IllegalArgumentException("La pregunta requiere opciones de respuesta");
        }
        this.distractors = distractors;
    }

    public QuestionState getState() {
        return state;
    }

    /**
     * Regla de negocio: una pregunta eliminada no se puede reactivar y el nuevo
     * estado no puede ser nulo.
     *
     * @param newState estado al que se quiere mover la pregunta.
     * @throws IllegalArgumentException si el estado es nulo.
     * @throws IllegalStateException    si la pregunta ya fue eliminada.
     */
    public void changeState(QuestionState newState) {
        if (newState == null) {
            throw new IllegalArgumentException("El nuevo estado es obligatorio");
        }
        if (this.state == QuestionState.ELIMINADA && newState != QuestionState.ELIMINADA) {
            throw new IllegalStateException("Una pregunta eliminada no puede cambiar de estado");
        }
        this.state = newState;
    }

    /**
     * @return texto de la respuesta correcta, delegado al objeto de opciones.
     */
    public String getCorrectAnswer() {
        return distractors.getCorrectAnswer();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Question)) {
            return false;
        }
        return id.equals(((Question) other).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * @return representacion usada por el comboBox de la vista principal.
     */
    @Override
    public String toString() {
        return id + " - " + name;
    }
}
