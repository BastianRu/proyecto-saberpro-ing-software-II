package com.unicauca.lisw2t2g08.domain;

import java.util.Locale;

/**
 * Capa de dominio.
 *
 * Criterios opcionales para consultar preguntas (RF-07). Un criterio nulo o
 * vacio no restringe la busqueda; los textos se comparan sin distinguir
 * mayusculas y el texto libre se busca en el nombre, contexto y pregunta.
 */
public class QuestionCriteria {

    private QuestionState state;
    private DifficultyLevel difficulty;
    private String competence;
    private String topic;
    private String subtopic;
    private String text;

    public QuestionState getState() {
        return state;
    }

    public QuestionCriteria withState(QuestionState state) {
        this.state = state;
        return this;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public QuestionCriteria withDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    public String getCompetence() {
        return competence;
    }

    public QuestionCriteria withCompetence(String competence) {
        this.competence = competence;
        return this;
    }

    public String getTopic() {
        return topic;
    }

    public QuestionCriteria withTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public String getSubtopic() {
        return subtopic;
    }

    public QuestionCriteria withSubtopic(String subtopic) {
        this.subtopic = subtopic;
        return this;
    }

    public String getText() {
        return text;
    }

    public QuestionCriteria withText(String text) {
        this.text = text;
        return this;
    }

    /**
     * @param question pregunta a evaluar.
     * @return true si cumple todos los criterios informados.
     */
    public boolean matches(Question question) {
        if (state != null && question.getState() != state) {
            return false;
        }
        if (difficulty != null && question.getDifficulty() != difficulty) {
            return false;
        }
        if (!sameText(competence, question.getCompetence())
                || !sameText(topic, question.getTopic())
                || !sameText(subtopic, question.getSubtopic())) {
            return false;
        }
        if (isBlank(text)) {
            return true;
        }
        String needle = normalize(text);
        return normalize(question.getName()).contains(needle)
                || normalize(question.getContext()).contains(needle)
                || normalize(question.getStatement()).contains(needle);
    }

    private static boolean sameText(String wanted, String actual) {
        return isBlank(wanted) || normalize(wanted).equals(normalize(actual));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
