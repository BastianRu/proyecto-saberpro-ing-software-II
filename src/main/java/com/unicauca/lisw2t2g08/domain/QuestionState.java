package com.unicauca.lisw2t2g08.domain;

/**
 * Capa de dominio.
 *
 * Estados validos del ciclo de vida de una pregunta del banco. Usar un enum en
 * lugar de cadenas evita estados invalidos en tiempo de compilacion.
 */
public enum QuestionState {

    BORRADOR("Borrador"),
    PENDIENTE_REVISION("Pendiente de revision"),
    ELIMINADA("Eliminada");

    private final String label;

    QuestionState(String label) {
        this.label = label;
    }

    /**
     * @return nombre legible del estado, para presentarlo en la interfaz.
     */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
