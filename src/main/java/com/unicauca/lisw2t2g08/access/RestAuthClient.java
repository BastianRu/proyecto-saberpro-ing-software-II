package com.unicauca.lisw2t2g08.access;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicauca.lisw2t2g08.domain.Role;
import com.unicauca.lisw2t2g08.domain.Session;
import com.unicauca.lisw2t2g08.service.AuthServiceUnavailableException;
import com.unicauca.lisw2t2g08.service.AuthenticationException;
import com.unicauca.lisw2t2g08.service.IAuthClient;
import com.unicauca.lisw2t2g08.service.InvalidPasswordException;
import com.unicauca.lisw2t2g08.service.UserAlreadyExistsException;


// Implementacion de IAuthClient que consume la API REST del servicio de autenticacion.
// Es la unica clase del cliente que sabe de HTTP y JSON para usuarios.


public class RestAuthClient implements IAuthClient {

  private static final Duration TIMEOUT = Duration.ofSeconds(5);

  private final String baseUrl;
  private final HttpClient http = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
  private final ObjectMapper json = new ObjectMapper();

  public RestAuthClient(String baseUrl) {
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
  }


  @Override
  public void register(String login, String fullName, Role role, String rawPassword) {
    Map<String, String> body = new java.util.HashMap<>();
    body.put("login", login);
    body.put("fullName", fullName);
    body.put("role", role.name());
    body.put("password", rawPassword);

    HttpResponse<String> response = post("/usuarios", body);
    JsonNode payload = parse(response.body());

    switch (response.statusCode()) {
      case 201 -> { }
      case 409 -> throw new UserAlreadyExistsException(payload.path("message").asText());
      case 400 -> {
        List<String> errors = new ArrayList<>();
        payload.path("errors").forEach(e -> errors.add(e.asText()));
        if (errors.isEmpty()) {
          throw new IllegalArgumentException(payload.path("message").asText("Solicitud inválida"));
        }
        throw new InvalidPasswordException(errors);
      }
      default -> throw unexpected(response);
    }
  }


  @Override
  public Session login(String login, String rawPassword) {
    Map<String, String> body = new java.util.HashMap<>();
    body.put("login", login);
    body.put("password", rawPassword);

    HttpResponse<String> response = post("/login", body);
    JsonNode payload = parse(response.body());

    return switch (response.statusCode()) {
      case 200 -> new Session(payload.path("subject").asText(),
              Role.valueOf(payload.path("role").asText()), payload.path("token").asText());
      case 401 -> throw new AuthenticationException(payload.path("message").asText());
      case 400 -> throw new AuthenticationException("Usuario o contraseña incorrectos");
      default -> throw unexpected(response);
    };
  }


  private HttpResponse<String> post(String path, Map<String, String> body) {
    try {
      HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
              .timeout(TIMEOUT)
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)))
              .build();
      return http.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AuthServiceUnavailableException("Se interrumpió la conexión con el servicio de autenticación", e);
    } catch (IOException e) {
      throw new AuthServiceUnavailableException(
              "No se pudo contactar el servicio de autenticación en " + baseUrl, e);
    }
  }

  private JsonNode parse(String body) {
    try {
      return body == null || body.isBlank() ? json.createObjectNode() : json.readTree(body);
    } catch (IOException e) {
      return json.createObjectNode(); // respuesta no JSON: se trata como error inesperado
    }
  }

  private static IllegalStateException unexpected(HttpResponse<String> response) {
    return new IllegalStateException(
            "Respuesta inesperada del servicio de autenticación (HTTP " + response.statusCode() + ")");
  }
}
