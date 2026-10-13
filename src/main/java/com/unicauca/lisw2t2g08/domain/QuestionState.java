package com.unicauca.lisw2t2g08.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Capa de dominio.
 *
 * Estados validos del ciclo de vida de una pregunta del banco (HU04). Usar un
 * enum en lugar de cadenas evita estados invalidos en tiempo de compilacion.
 *
 * Ademas de enumerar los estados, cada constante conoce las transiciones que
 * permite. Asi se modela la maquina de estados del ciclo de vida (HU04-02) con
 * el comportamiento propio de cada estado, sin condicionales anidados en las
 * entidades ni en los servicios.
 */
public enum QuestionState {

    BORRADOR("Borrador"),
    PENDIENTE_REVISION("Pendiente de revisión"),
    EN_REVISION("En revisión"),
    APROBADA("Aprobada"),
    RECHAZADA("Rechazada"),
    PUBLICADA("Publicada"),
    ARCHIVADA("Archivada");

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

    /**
     * Transiciones permitidas desde este estado. Se resuelve con un switch por
     * estado (y no con campos EnumSet estaticos) para evitar problemas de orden
     * de inicializacion de las constantes del enum.
     *
     * @return conjunto de estados a los que se puede pasar desde este.
     */
    public Set<QuestionState> allowedTransitions() {
        return switch (this) {
            case BORRADOR -> EnumSet.of(PENDIENTE_REVISION);
            case PENDIENTE_REVISION -> EnumSet.of(EN_REVISION, BORRADOR);
            case EN_REVISION -> EnumSet.of(APROBADA, RECHAZADA);
            case RECHAZADA -> EnumSet.of(BORRADOR, ARCHIVADA);
            case APROBADA -> EnumSet.of(PUBLICADA, ARCHIVADA);
            case PUBLICADA -> EnumSet.of(ARCHIVADA);
            case ARCHIVADA -> EnumSet.noneOf(QuestionState.class);
        };
    }

    /**
     * @param target estado destino.
     * @return true si la transicion desde este estado hacia {@code target} esta
     *         definida en la maquina de estados.
     */
    public boolean canTransitionTo(QuestionState target) {
        return target != null && allowedTransitions().contains(target);
    }

    /**
     * Estados que la interfaz puede ofrecer como destino: el estado actual (sin
     * cambio) mas las transiciones validas. Un estado final solo ofrece el suyo.
     *
     * @return conjunto de estados seleccionables desde este.
     */
    public Set<QuestionState> selectableTransitions() {
        EnumSet<QuestionState> options = EnumSet.of(this);
        options.addAll(allowedTransitions());
        return options;
    }

    /**
     * @return true si el estado es final (no admite mas transiciones).
     */
    public boolean isFinal() {
        return allowedTransitions().isEmpty();
    }

    @Override
    public String toString() {
        return label;
    }
}
