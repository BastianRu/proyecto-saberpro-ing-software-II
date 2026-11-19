package com.unicauca.lisw2t2g08.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.unicauca.lisw2t2g08.auth.access.IUserRepository;
import com.unicauca.lisw2t2g08.auth.domain.AuthToken;
import com.unicauca.lisw2t2g08.auth.domain.IPasswordHasher;
import com.unicauca.lisw2t2g08.auth.domain.Role;
import com.unicauca.lisw2t2g08.auth.domain.User;
import com.unicauca.lisw2t2g08.auth.domain.UserStatus;

class UserServiceTest {

    private IUserRepository repository;
    private TokenSigner signer;
    private UserService service;

    @BeforeEach
    void setUp() {
        repository = new FakeUserRepository();
        IPasswordHasher hasher = new IPasswordHasher() {
            public String hash(String p) { return "h:" + p; }
            public boolean matches(String p, String h) { return ("h:" + p).equals(h); }
        };
        Clock clock = Clock.fixed(Instant.parse("2026-10-31T10:00:00Z"), ZoneOffset.UTC);
        signer = new TokenSigner("secreto-de-prueba-123456", Duration.ofMinutes(30), clock);
        service = new UserService(repository, hasher, signer);
    }

    @Test
    void registraUsuarioValidoComoActivoYSinGuardarLaClaveEnClaro() {
        User user = service.register("jperez", "Juan Pérez", Role.DOCENTE, "Clave123!");

        assertEquals(UserStatus.ACTIVO, user.getStatus());
        assertNotEquals("Clave123!", user.getHashedPassword());
    }

    @Test
    void rechazaLoginDuplicado() {
        service.register("jperez", "Juan Pérez", Role.DOCENTE, "Clave123!");
        assertThrows(UserAlreadyExistsException.class,
                () -> service.register("jperez", "Otro", Role.REVISOR, "Clave123!"));
    }

    @Test
    void rechazaContrasenaQueIncumplePolitica() {
        InvalidPasswordException e = assertThrows(InvalidPasswordException.class,
                () -> service.register("jperez", "Juan", Role.DOCENTE, "abc"));
        assertTrue(e.getErrors().size() > 1);
    }

    @Test
    void rechazaLoginConSeparadorDelToken() {
        assertThrows(IllegalArgumentException.class,
                () -> service.register("a|b", "Juan", Role.DOCENTE, "Clave123!"));
    }

    @Test
    void loginCorrectoEmiteTokenConRolYSujeto() {
        service.register("jperez", "Juan Pérez", Role.REVISOR, "Clave123!");

        AuthToken token = service.login("jperez", "Clave123!");

        assertEquals("jperez", token.getSubject());
        assertEquals(Role.REVISOR, token.getRole());
        Optional<AuthToken> verified = signer.verify(signer.compact(token));
        assertTrue(verified.isPresent());
    }

    @Test
    void loginFallaConClaveIncorrecta() {
        service.register("jperez", "Juan Pérez", Role.REVISOR, "Clave123!");
        assertThrows(AuthenticationException.class, () -> service.login("jperez", "Otra123!"));
    }

    @Test
    void loginFallaConUsuarioInexistenteConElMismoMensaje() {
        service.register("jperez", "Juan Pérez", Role.REVISOR, "Clave123!");
        AuthenticationException noUser = assertThrows(AuthenticationException.class,
                () -> service.login("nadie", "Clave123!"));
        AuthenticationException badPass = assertThrows(AuthenticationException.class,
                () -> service.login("jperez", "Otra123!"));
        assertEquals(badPass.getMessage(), noUser.getMessage());
    }

    @Test
    void loginFallaConUsuarioInactivo() {
        User user = service.register("jperez", "Juan Pérez", Role.REVISOR, "Clave123!");
        user.setStatus(UserStatus.INACTIVO);
        assertThrows(AuthenticationException.class, () -> service.login("jperez", "Clave123!"));
    }
}
