package com.unicauca.lisw2t2g08.banco.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.unicauca.lisw2t2g08.banco.domain.DifficultyLevel;
import com.unicauca.lisw2t2g08.banco.domain.QuestionDraft;

/**
 * Pruebas de la comunicacion con validacion-service.
 *
 * Se usan servidores HTTP locales de la JDK para no depender de que el
 * microservicio real este en marcha durante las pruebas.
 */
class RestValidationClientTest {

    @Test
    void servicioCaidoLanzaValidationServiceUnavailableException() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        int port = server.getAddress().getPort();
        server.start();
        server.stop(0);

        RestValidationClient client = new RestValidationClient("http://127.0.0.1:" + port);

        ValidationServiceUnavailableException ex = assertThrows(
                ValidationServiceUnavailableException.class, () -> client.validate(validDraft()));

        assertTrue(ex.getMessage().contains("No fue posible comunicarse con el servicio de validacion"));
    }

    @Test
    void tiempoAgotadoLanzaValidationServiceUnavailableException() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/validaciones", exchange -> {
            try {
                Thread.sleep(600);
                byte[] body = "{\"valid\":true,\"errors\":[]}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException ignored) {
                // El cliente ya cerro la conexion por el tiempo de espera
            } finally {
                exchange.close();
            }
        });
        server.start();

        try {
            RestValidationClient client = new RestValidationClient(
                    "http://127.0.0.1:" + server.getAddress().getPort(),
                    HttpClient.newBuilder().connectTimeout(Duration.ofMillis(200)).build(),
                    new ObjectMapper(),
                    Duration.ofMillis(100));

            assertThrows(ValidationServiceUnavailableException.class, () -> client.validate(validDraft()));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void respuestaDeReglasEsValidationException() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/validaciones", exchange -> {
            byte[] body = "{\"valid\":false,\"errors\":[\"RF-08: el contexto es obligatorio\"]}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(422, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        try {
            RestValidationClient client = new RestValidationClient(
                    "http://127.0.0.1:" + server.getAddress().getPort());

            ValidationException ex = assertThrows(ValidationException.class,
                    () -> client.validate(validDraft()));

            assertEquals(1, ex.getErrors().size());
            assertTrue(ex.getErrors().get(0).startsWith("RF-08"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void respuestaExitosaNoLanzaExcepcion() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/validaciones", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] body = "{\"valid\":true,\"errors\":[]}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        try {
            RestValidationClient client = new RestValidationClient(
                    "http://127.0.0.1:" + server.getAddress().getPort());

            assertDoesNotThrow(() -> client.validate(validDraft()));
        } finally {
            server.stop(0);
        }
    }

    private QuestionDraft validDraft() {
        QuestionDraft draft = new QuestionDraft();
        draft.setName("Ventaja del patron Pipeline");
        draft.setContext("El patron Pipeline encadena filtros que procesan datos en secuencia.");
        draft.setDirectQuestion("Cual es la ventaja principal de este patron?");
        draft.setOptions(java.util.List.of(
                "Permite componer validaciones independientes",
                "Obliga a reescribir todo el sistema",
                "Elimina la necesidad de pruebas",
                "Impide reutilizar componentes",
                "Requiere una base de datos distribuida"));
        draft.setCorrectIndex(0);
        draft.setJustification("El patron permite componer validaciones independientes.");
        draft.setBibliography("Gamma et al. Design Patterns.");
        draft.setCompetence("Diseno de software");
        draft.setTopic("Patrones de diseno");
        draft.setSubtopic("Pipeline");
        draft.setDifficulty(DifficultyLevel.MEDIA);
        return draft;
    }
}
