package com.unicauca.lisw2t2g08.banco.domain;

import java.util.List;

/**
 * Fábrica y registro de los estados (Flyweight). Los estados no guardan datos
 * por pregunta, así que existe una única instancia de cada uno y se compara por
 * identidad.
 */
public final class QuestionStates {

    public static final QuestionState BORRADOR = new BorradorState();
    public static final QuestionState PENDIENTE_REVISION = new PendienteRevisionState();
    public static final QuestionState EN_REVISION = new EnRevisionState();
    public static final QuestionState APROBADA = new AprobadaState();
    public static final QuestionState RECHAZADA = new RechazadaState();
    public static final QuestionState PUBLICADA = new PublicadaState();
    public static final QuestionState ARCHIVADA = new ArchivadaState();

    private static final List<QuestionState> ALL = List.of(
            BORRADOR, PENDIENTE_REVISION, EN_REVISION, APROBADA, RECHAZADA, PUBLICADA, ARCHIVADA);

    private QuestionStates() {
    }

    /** @return los siete estados, en el orden del ciclo de vida. */
    public static List<QuestionState> all() {
        return ALL;
    }

    /**
     * @param code identificador del estado, por ejemplo "BORRADOR".
     * @return el estado con ese identificador.
     * @throws IllegalArgumentException si el código no corresponde a ningún estado.
     */
    public static QuestionState fromCode(String code) {
        if (code != null) {
            for (QuestionState state : ALL) {
                if (state.getCode().equals(code.trim())) {
                    return state;
                }
            }
        }
        throw new IllegalArgumentException("Estado desconocido: " + code);
    }
}
