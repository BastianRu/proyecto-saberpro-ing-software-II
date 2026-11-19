package com.unicauca.lisw2t2g08.auditoria.access;

import java.util.List;

import com.unicauca.lisw2t2g08.auditoria.domain.AuditRecord;

public interface IAuditRepository {

    /**
     * Guarda el registro solo si no se había procesado ya un evento con ese identificador.
     *
     * @return true si se guardó; false si era un duplicado (entrega repetida del broker).
     */
    boolean saveIfNew(AuditRecord record);

    /** @return historial de la pregunta, del evento más antiguo al más reciente. */
    List<AuditRecord> findByQuestionId(String questionId);
}
