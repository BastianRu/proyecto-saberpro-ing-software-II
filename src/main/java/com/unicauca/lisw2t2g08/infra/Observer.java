package com.unicauca.lisw2t2g08.infra;

/**
 * Capa transversal (infra).
 * Contrato del observador dentro del patron Observer. Cualquier vista que deba
 * reaccionar al cambio de estado de un sujeto implementa esta interfaz.
 */
public interface Observer {

    /**
     * Invocado por el sujeto cuando su estado cambia.
     *
     * @param data informacion publicada por el sujeto (puede ser null).
     */
    void update(Object data);
}
