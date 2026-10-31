package com.unicauca.lisw2t2g08.access;

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
import com.unicauca.lisw2t2g08.domain.Role;
import com.unicauca.lisw2t2g08.domain.Session;
import com.unicauca.lisw2t2g08.service.AuthServiceUnavailableException;
import com.unicauca.lisw2t2g08.service.AuthenticationException;
import com.unicauca.lisw2t2g08.service.InvalidPasswordException;
import com.unicauca.lisw2t2g08.service.UserAlreadyExistsException;

/**
 * Pruebas de RestAuthClient contra un servidor HTTP falso del JDK que imita
 * el contrato del servicio de autenticacion (sin levantar el servicio real).
 */
class RestAuthClientTest {

    private HttpServer server;
    private RestAuthClient client;
    private final AtomicReference<String> lastBody = new AtomicReference<>();

    private void respond(String path, int status, String body) {
        server.createContext(path, exchange -> {
            lastBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
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
        client = new RestAuthClient("http://localhost:" + server.getAddress().getPort());
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void loginExitosoDevuelveLaSesionConElToken() {
        respond("/login", 200,
                "{\"token\":\"abc.def\",\"subject\":\"ana\",\"role\":\"REVISOR\",\"expiresAt\":\"2026-10-31T10:00:00\"}");

        Session session = client.login("ana", "Clave123!");

        assertEquals("ana", session.getLogin());
        assertEquals(Role.REVISOR, session.getRole());
        assertEquals("abc.def", session.getToken());
        assertTrue(lastBody.get().contains("\"login\":\"ana\""));
    }

    @Test
    void login401SeConvierteEnAuthenticationException() {
        respond("/login", 401, "{\"message\":\"Usuario o contraseña incorrectos\",\"errors\":[]}");

        AuthenticationException e = assertThrows(AuthenticationException.class,
                () -> client.login("ana", "mala"));
        assertEquals("Usuario o contraseña incorrectos", e.getMessage());
    }

    @Test
    void registroExitosoEnviaElRolComoTexto() {
        respond("/usuarios", 201, "{\"id\":1,\"login\":\"ana\"}");

        client.register("ana", "Ana Ruiz", Role.DOCENTE, "Clave123!");

        assertTrue(lastBody.get().contains("\"role\":\"DOCENTE\""));
    }

    @Test
    void registro409SeConvierteEnUserAlreadyExists() {
        respond("/usuarios", 409, "{\"message\":\"Ya existe un usuario con el login: ana\",\"errors\":[]}");

        assertThrows(UserAlreadyExistsException.class,
                () -> client.register("ana", "Ana", Role.DOCENTE, "Clave123!"));
    }

    @Test
    void registro400ConErroresSeConvierteEnInvalidPassword() {
        respond("/usuarios", 400, "{\"message\":\"x\",\"errors\":[\"Falta un dígito\",\"Falta una mayúscula\"]}");

        InvalidPasswordException e = assertThrows(InvalidPasswordException.class,
                () -> client.register("ana", "Ana", Role.DOCENTE, "abc"));
        assertEquals(2, e.getErrors().size());
    }

    @Test
    void servicioCaidoLanzaAuthServiceUnavailable() {
        server.stop(0);
        assertThrows(AuthServiceUnavailableException.class, () -> client.login("ana", "Clave123!"));
    }
}
