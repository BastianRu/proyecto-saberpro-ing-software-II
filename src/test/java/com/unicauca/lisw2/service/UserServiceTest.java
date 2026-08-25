package com.unicauca.lisw2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.unicauca.lisw2.access.IUserRepository;
import com.unicauca.lisw2.domain.IPasswordHasher;
import com.unicauca.lisw2.domain.Role;
import com.unicauca.lisw2.domain.User;
import com.unicauca.lisw2.domain.UserStatus;

/**
 * Pruebas de la lógica de negocio de UserService, usando FakeUserRepository
 * y FakePasswordHasher en vez de las implementaciones reales.
 */
class UserServiceTest {

    private IUserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = new FakeUserRepository();
        IPasswordHasher passwordHasher = new FakePasswordHasher();
        userService = new UserService(userRepository, passwordHasher);
    }

    @Test
    void deberiaRegistrarUsuarioValido() {
        User user = userService.register("jperez", "Juan Pérez", Role.ESTUDIANTE, "Clave123!");

        assertNotNull(user.getId());
        assertEquals("jperez", user.getLogin());
        assertEquals(UserStatus.ACTIVO, user.getStatus());
        assertNotEquals("Clave123!", user.getHashedPassword()); // nunca se guarda en texto plano
    }

    @Test
    void noDeberiaRegistrarLoginDuplicado() {
        userService.register("jperez", "Juan Pérez", Role.ESTUDIANTE, "Clave123!");

        assertThrows(UserAlreadyExistsException.class, () ->
                userService.register("jperez", "Otro Usuario", Role.DOCENTE, "OtraClave1!"));
    }

    @Test
    void noDeberiaRegistrarContrasenaInvalida() {
        InvalidPasswordException ex = assertThrows(InvalidPasswordException.class, () ->
                userService.register("jperez", "Juan Pérez", Role.ESTUDIANTE, "corta"));

        assertFalse(ex.getErrors().isEmpty());
    }

    @Test
    void deberiaIniciarSesionConCredencialesCorrectas() {
        userService.register("jperez", "Juan Pérez", Role.ESTUDIANTE, "Clave123!");

        User user = userService.login("jperez", "Clave123!");

        assertEquals("jperez", user.getLogin());
    }

    @Test
    void noDeberiaIniciarSesionConContrasenaIncorrecta() {
        userService.register("jperez", "Juan Pérez", Role.ESTUDIANTE, "Clave123!");

        assertThrows(AuthenticationException.class, () ->
                userService.login("jperez", "ClaveIncorrecta1!"));
    }

    @Test
    void noDeberiaIniciarSesionConLoginInexistente() {
        assertThrows(AuthenticationException.class, () ->
                userService.login("noexiste", "Clave123!"));
    }

    @Test
    void noDeberiaIniciarSesionSiUsuarioEstaInactivo() {
        User user = userService.register("jperez", "Juan Pérez", Role.ESTUDIANTE, "Clave123!");
        user.setStatus(UserStatus.INACTIVO);
        userRepository.update(user);

        assertThrows(AuthenticationException.class, () ->
                userService.login("jperez", "Clave123!"));
    }

    @Test
    void listUsersDeberiaRetornarTodosLosRegistrados() {
        userService.register("jperez", "Juan Pérez", Role.ESTUDIANTE, "Clave123!");
        userService.register("mgomez", "María Gómez", Role.DOCENTE, "Clave456!");

        assertEquals(2, userService.listUsers().size());
    }
}