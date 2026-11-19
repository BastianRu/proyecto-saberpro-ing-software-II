package com.unicauca.lisw2t2g08.revision.domain;

/** Estado de una revisión: PENDIENTE -> EN_CURSO -> COMPLETADA, o CANCELADA si el autor retira la pregunta. */
public enum ReviewStatus {
    PENDIENTE,
    EN_CURSO,
    COMPLETADA,
    CANCELADA
}
