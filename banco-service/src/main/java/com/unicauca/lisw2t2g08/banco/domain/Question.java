package com.unicauca.lisw2t2g08.banco.domain;

import java.util.Objects;

/**
 * Entidad principal del banco de preguntas. Encapsula sus reglas de negocio:
 * identidad obligatoria, datos no vacios y transiciones de estado validas.
 */
public class Question {

    private final String id;
    private String name;
    private String statement;
    private QuestionDistractors distractors;
    private QuestionState state;
    private String context = "";
    private String justification = "";
    private String bibliography = "";
    private String competence = "";
    private String topic = "";
    private String subtopic = "";
    private DifficultyLevel difficulty;

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

    public Question(String id, String name, String context, String statement,
                    QuestionDistractors distractors, String justification,
                    String bibliography, String competence, String topic,
                    String subtopic, DifficultyLevel difficulty, QuestionState state) {
        this(id, name, statement, distractors, state);
        this.context = nullToEmpty(context);
        this.justification = nullToEmpty(justification);
        this.bibliography = nullToEmpty(bibliography);
        this.competence = nullToEmpty(competence);
        this.topic = nullToEmpty(topic);
        this.subtopic = nullToEmpty(subtopic);
        this.difficulty = difficulty;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
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

    public String getContext() {
        return context;
    }

    public String getJustification() {
        return justification;
    }

    public String getBibliography() {
        return bibliography;
    }

    public String getCompetence() {
        return competence;
    }

    public String getTopic() {
        return topic;
    }

    public String getSubtopic() {
        return subtopic;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    /**
     * Reemplaza el contenido editable de la pregunta, solo si esta en estado Borrador.
     */
    public void modifyContent(String name, String context, String statement,
                              QuestionDistractors distractors, String justification,
                              String bibliography, String competence, String topic,
                              String subtopic, DifficultyLevel difficulty) {
        if (state != QuestionState.BORRADOR) {
            throw new IllegalStateException("Solo se pueden modificar preguntas en estado Borrador");
        }
        Question candidate = new Question(id, name, context, statement, distractors,
                justification, bibliography, competence, topic, subtopic, difficulty, state);
        this.name = candidate.name;
        this.context = candidate.context;
        this.statement = candidate.statement;
        this.distractors = candidate.distractors;
        this.justification = candidate.justification;
        this.bibliography = candidate.bibliography;
        this.competence = candidate.competence;
        this.topic = candidate.topic;
        this.subtopic = candidate.subtopic;
        this.difficulty = candidate.difficulty;
    }

    /**
     * Aplica la transicion de estado segun la maquina de estados.
     */
    public void changeState(QuestionState newState) {
        if (newState == null) {
            throw new IllegalArgumentException("El nuevo estado es obligatorio");
        }
        if (newState == this.state) {
            return;
        }
        if (!state.canTransitionTo(newState)) {
            throw new IllegalStateException("Transición no permitida de "
                    + state.getLabel() + " a " + newState.getLabel());
        }
        this.state = newState;
    }

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

    @Override
    public String toString() {
        return id + " - " + name;
    }
}
