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
    private String context = "";
    private String justification = "";
    private String bibliography = "";
    private String competence = "";
    private String topic = "";
    private String subtopic = "";
    private DifficultyLevel difficulty;

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

    /**
     * Constructor completo con los campos de RF-05. El enunciado corresponde a
     * la pregunta directa. Los textos nulos se guardan como cadena vacia; la
     * exigencia de que esten completos la aplica la tuberia de validacion.
     */
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
     * RF-06: reemplaza el contenido editable de la pregunta, solo si esta en
     * estado Borrador. El id y el estado no cambian.
     *
     * @throws IllegalStateException    si la pregunta no esta en Borrador.
     * @throws IllegalArgumentException si falta el nombre, la pregunta o las opciones.
     */
    public void modifyContent(String name, String context, String statement,
                              QuestionDistractors distractors, String justification,
                              String bibliography, String competence, String topic,
                              String subtopic, DifficultyLevel difficulty) {
        if (state != QuestionState.BORRADOR) {
            throw new IllegalStateException(
                    "Solo se pueden modificar preguntas en estado Borrador");
        }
        // Se validan todos los datos antes de asignar para no dejar la pregunta a medias
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
     * Regla de negocio: solo se permiten las transiciones definidas en la
     * maquina de estados del ciclo de vida (HU04-02) y el nuevo estado no puede
     * ser nulo. Pasar al mismo estado se acepta como una operacion sin efecto.
     *
     * @param newState estado al que se quiere mover la pregunta.
     * @throws IllegalArgumentException si el estado es nulo.
     * @throws IllegalStateException    si la transicion no es valida.
     */
    public void changeState(QuestionState newState) {
        if (newState == null) {
            throw new IllegalArgumentException("El nuevo estado es obligatorio");
        }
        if (newState == this.state) {
            return;
        }
        if (!state.canTransitionTo(newState)) {
            throw new IllegalStateException("Transicion no permitida de "
                    + state.getLabel() + " a " + newState.getLabel());
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
