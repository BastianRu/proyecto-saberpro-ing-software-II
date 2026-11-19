package com.unicauca.lisw2t2g08.revision.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Implementación de IBancoClient sobre HTTP (java.net.http) y JSON (Jackson). */
public class RestBancoClient implements IBancoClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final String baseUrl;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
    private final ObjectMapper json = new ObjectMapper();

    public RestBancoClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @Override
    public void changeState(String questionId, String targetState, String bearerToken) {
        HttpResponse<String> response;
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/preguntas/" + questionId + "/estado"))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + bearerToken)
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(json.writeValueAsString(Map.of("state", targetState))))
                    .build();
            response = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BancoUnavailableException("Se interrumpió la conexión con el Banco", e);
        } catch (IOException e) {
            throw new BancoUnavailableException("No se pudo contactar el Banco en " + baseUrl, e);
        }

        switch (response.statusCode()) {
            case 200 -> { }
            case 400, 404, 409, 422 -> throw new BancoRejectedException(messageOf(response));
            default -> throw new BancoUnavailableException(
                    "Respuesta inesperada del Banco (HTTP " + response.statusCode() + ")", null);
        }
    }

    private String messageOf(HttpResponse<String> response) {
        try {
            JsonNode body = json.readTree(response.body());
            return body.path("message").asText("El Banco rechazó la operación (HTTP " + response.statusCode() + ")");
        } catch (IOException e) {
            return "El Banco rechazó la operación (HTTP " + response.statusCode() + ")";
        }
    }
}
