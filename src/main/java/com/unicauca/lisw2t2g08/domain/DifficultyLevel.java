package com.unicauca.lisw2t2g08.domain;

/**
 * Capa de dominio.
 *
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

    /**
     * @return nombre legible del nivel, para presentarlo en la interfaz.
     */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
