package com.unicauca.lisw2t2g08.revision.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

/** RestBancoClient contra un servidor HTTP falso del JDK que imita el contrato del Banco. */
class RestBancoClientTest {

    private HttpServer server;
    private RestBancoClient client;
    private final AtomicReference<String> body = new AtomicReference<>();
    private final AtomicReference<String> auth = new AtomicReference<>();
    private final AtomicReference<String> method = new AtomicReference<>();
    private final AtomicReference<String> path = new AtomicReference<>();

    private void respond(int status, String responseBody) {
        server.createContext("/preguntas", exchange -> {
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            method.set(exchange.getRequestMethod());
            path.set(exchange.getRequestURI().getPath());
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
    }

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.start();
        client = new RestBancoClient("http://localhost:" + server.getAddress().getPort());
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void enviaPatchConElEstadoYElTokenDelRevisor() {
        respond(200, "{}");

        client.changeState("P-001", "EN_REVISION", "tok.sig");

        assertEquals("PATCH", method.get());
        assertEquals("/preguntas/P-001/estado", path.get());
        assertEquals("Bearer tok.sig", auth.get());
        assertTrue(body.get().contains("\"state\":\"EN_REVISION\""));
    }

    @Test
    void unConflictoDelBancoSeConvierteEnRechazoConSuMensaje() {
        respond(409, "{\"message\":\"Transicion no permitida de Borrador a Publicada\",\"errors\":[]}");

        BancoRejectedException e = assertThrows(BancoRejectedException.class,
                () -> client.changeState("P-001", "PUBLICADA", "t"));
        assertEquals("Transicion no permitida de Borrador a Publicada", e.getMessage());
    }

    @Test
    void preguntaInexistenteSeConvierteEnRechazo() {
        respond(404, "{\"message\":\"La pregunta no existe en el banco: P-9\",\"errors\":[]}");
        assertThrows(BancoRejectedException.class, () -> client.changeState("P-9", "EN_REVISION", "t"));
    }

    @Test
    void unErrorDelServidorEsIndisponibilidad() {
        respond(500, "{}");
        assertThrows(BancoUnavailableException.class, () -> client.changeState("P-001", "EN_REVISION", "t"));
    }

    @Test
    void bancoCaidoEsIndisponibilidad() {
        server.stop(0);
        assertThrows(BancoUnavailableException.class, () -> client.changeState("P-001", "EN_REVISION", "t"));
    }
}
