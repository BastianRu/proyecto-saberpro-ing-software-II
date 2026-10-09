package com.unicauca.lisw2t2g08.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Capa de dominio.
 *
 * Borrador "crudo" de una pregunta nueva, tal como llega desde la interfaz.
 * Es deliberadamente permisivo (sin invariantes): su proposito es atravesar la
 * tuberia de filtros de HE 3 (validacion estructural), que es quien aplica las
 * reglas RF-08 a RF-13.
 *
 * Las opciones incluyen la respuesta correcta y los distractores; la posicion
 * de la correcta se indica con {@code correctIndex}.
 */
public class QuestionDraft {

    private String context;
    private String directQuestion;
    private final List<String> options = new ArrayList<>();
    private int correctIndex = -1;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getDirectQuestion() {
        return directQuestion;
    }

    public void setDirectQuestion(String directQuestion) {
        this.directQuestion = directQuestion;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> newOptions) {
        options.clear();
        if (newOptions != null) {
            options.addAll(newOptions);
        }
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public void setCorrectIndex(int correctIndex) {
        this.correctIndex = correctIndex;
    }
}
