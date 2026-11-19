package com.unicauca.lisw2t2g08.banco.domain;

import java.util.List;

/**
 * Patrón State (GoF). Rol: State.
 *
 * Cada estado del ciclo de vida de una pregunta es una subclase. La pregunta
 * (Context) delega en su estado actual cada operación de negocio; el estado
 * decide si la operación es legal y, de serlo, ordena el cambio de estado.
 *
 * Las operaciones por defecto lanzan {@link InvalidStateOperationException}:
 * cada subclase sobrescribe únicamente las que le son válidas, de modo que lo
 * no sobrescrito queda prohibido por construcción.
 *
 * Los estados no tienen atributos mutables, por eso se comparten como
 * singletons (patrón Flyweight) desde {@link QuestionStates}.
 */
public abstract class QuestionState {

    private final String code;
    private final String label;

    protected QuestionState(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /** @return identificador estable del estado, usado en la API, la base de datos y los eventos. */
    public String getCode() {
        return code;
    }

    /** @return nombre legible del estado, para presentarlo en la interfaz. */
    public String getLabel() {
        return label;
    }

    /** @return estados a los que se puede pasar desde este; vacío si es final. */
    public abstract List<QuestionState> allowedTransitions();

    /**
     * Doble despacho: la pregunta pide "quiero pasar a este estado" y cada estado
     * destino lo traduce a la operación de negocio correspondiente sobre el
     * estado actual de la pregunta. Evita un switch sobre el destino.
     */
    abstract void enter(Question question);

    /** @return true si el contenido de la pregunta puede modificarse en este estado. */
    public boolean isEditable() {
        return false;
    }

    /** @return true si el estado no admite más transiciones. */
    public boolean isFinal() {
        return allowedTransitions().isEmpty();
    }

    /** @return true si la transición hacia {@code target} está definida desde este estado. */
    public boolean canTransitionTo(QuestionState target) {
        return target != null && allowedTransitions().contains(target);
    }

    // Operaciones de negocio. Por defecto prohibidas; cada estado habilita las suyas.

    void submitForReview(Question question) {
        throw notAllowed(QuestionStates.PENDIENTE_REVISION);
    }

    void startReview(Question question) {
        throw notAllowed(QuestionStates.EN_REVISION);
    }

    void approve(Question question) {
        throw notAllowed(QuestionStates.APROBADA);
    }

    void reject(Question question) {
        throw notAllowed(QuestionStates.RECHAZADA);
    }

    void publish(Question question) {
        throw notAllowed(QuestionStates.PUBLICADA);
    }

    void archive(Question question) {
        throw notAllowed(QuestionStates.ARCHIVADA);
    }

    void returnToDraft(Question question) {
        throw notAllowed(QuestionStates.BORRADOR);
    }

    private InvalidStateOperationException notAllowed(QuestionState target) {
        return new InvalidStateOperationException(
                "Transicion no permitida de " + label + " a " + target.label);
    }

    @Override
    public String toString() {
        return label;
    }
}
