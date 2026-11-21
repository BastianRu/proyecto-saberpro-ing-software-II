package com.unicauca.lisw2t2g08.revision.service;

import java.util.ArrayList;
import java.util.List;

/** Doble del Banco: registra las llamadas y puede simular rechazo o caída. Solo pruebas. */
public class FakeBancoClient implements IBancoClient {

    public record Call(String questionId, String targetState, String token) { }

    public final List<Call> calls = new ArrayList<>();
    public RuntimeException failWith;

    @Override
    public void changeState(String questionId, String targetState, String bearerToken) {
        if (failWith != null) {
            throw failWith;
        }
        calls.add(new Call(questionId, targetState, bearerToken));
    }
}
