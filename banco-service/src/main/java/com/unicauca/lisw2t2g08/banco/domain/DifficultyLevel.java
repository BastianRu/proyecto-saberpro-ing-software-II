package com.unicauca.lisw2t2g08.banco.domain;

/**
 * Nivel de dificultad de una pregunta (RF-05).
 */
public enum DifficultyLevel {

    BAJA("Baja"),
    MEDIA("Media"),
    ALTA("Alta");

    private final String label;

    DifficultyLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
