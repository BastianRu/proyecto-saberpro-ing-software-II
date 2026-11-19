package com.unicauca.lisw2t2g08.revision.service;

/**
 * Puerto de salida hacia el Banco de Preguntas (REST síncrono). El servicio depende de esta
 * interfaz y no de HTTP (DIP): en pruebas se reemplaza por un doble en memoria.
 */
public interface IBancoClient {

    /**
     * Pide al Banco cambiar el estado de una pregunta. Se reenvía el token del revisor para que el
     * evento QuestionStateChanged y la auditoría queden a nombre de quien realmente actuó.
     *
     * @param targetState código del estado destino, por ejemplo "EN_REVISION".
     * @throws BancoRejectedException    si el Banco rechaza la transición (409), no existe la pregunta (404) o la solicitud es inválida.
     * @throws BancoUnavailableException si el Banco no responde.
     */
    void changeState(String questionId, String targetState, String bearerToken);
}
