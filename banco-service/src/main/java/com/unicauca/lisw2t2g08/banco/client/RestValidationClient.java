package com.unicauca.lisw2t2g08.banco.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicauca.lisw2t2g08.banco.domain.QuestionDraft;

/**
 * Implementacion REST de IValidationClient que consume la API de validacion-service.
 */
public class RestValidationClient implements IValidationClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public RestValidationClient(String baseUrl) {
        this(baseUrl, HttpClient.newBuilder().connectTimeout(TIMEOUT).build(), new ObjectMapper());
    }

    public RestValidationClient(String baseUrl, HttpClient httpClient, ObjectMapper objectMapper) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void validate(QuestionDraft draft) {
        if (draft == null) {
            throw new IllegalArgumentException("El borrador es obligatorio para la validacion");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("name", draft.getName());
        body.put("context", draft.getContext());
        body.put("directQuestion", draft.getDirectQuestion());
        body.put("options", draft.getOptions());
        body.put("correctIndex", draft.getCorrectIndex());
        body.put("justification", draft.getJustification());
        body.put("bibliography", draft.getBibliography());
        body.put("competence", draft.getCompetence());
        body.put("topic", draft.getTopic());
        body.put("subtopic", draft.getSubtopic());
        body.put("difficulty", draft.getDifficulty() == null ? null : draft.getDifficulty().name());

        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/validaciones"))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return; // Validacion exitosa
            }

            JsonNode root = objectMapper.readTree(response.body());
            List<String> errors = new ArrayList<>();
            if (root.has("errors") && root.get("errors").isArray()) {
                for (JsonNode err : root.get("errors")) {
                    errors.add(err.asText());
                }
            }
            if (errors.isEmpty()) {
                errors.add("Error de validacion en el servicio estructural (HTTP " + response.statusCode() + ")");
            }
            throw new ValidationException(errors);

        } catch (IOException e) {
            throw new IllegalStateException("No fue posible comunicarse con el servicio de validacion: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Peticion de validacion interrumpida", e);
        }
    }
}
