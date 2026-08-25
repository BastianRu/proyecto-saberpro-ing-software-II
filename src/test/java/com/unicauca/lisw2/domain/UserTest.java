package com.unicauca.lisw2.domain;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
 


class UserTest {
 
    @Test
    void deberiaCrearUsuarioValido() {
        User user = new User("jperez", "Juan Pérez", Role.ESTUDIANTE, UserStatus.ACTIVO, "hash123");
 
        assertEquals("jperez", user.getLogin());
        assertTrue(user.isActive());
    }
 
    @Test
    void deberiaRechazarLoginVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new User("", "Juan Pérez", Role.ESTUDIANTE, UserStatus.ACTIVO, "hash123"));
    }
 
    @Test
    void deberiaRechazarNombreCompletoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new User("jperez", "   ", Role.ESTUDIANTE, UserStatus.ACTIVO, "hash123"));
    }
 
    @Test
    void deberiaRechazarRolNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new User("jperez", "Juan Pérez", null, UserStatus.ACTIVO, "hash123"));
    }
 
    @Test
    void deberiaRechazarEstadoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new User("jperez", "Juan Pérez", Role.ESTUDIANTE, null, "hash123"));
    }
 
    @Test
    void deberiaRechazarHashDeContrasenaVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new User("jperez", "Juan Pérez", Role.ESTUDIANTE, UserStatus.ACTIVO, ""));
    }
 
    @Test
    void dosUsuariosConMismoLoginDeberianSerIguales() {
        User u1 = new User("jperez", "Juan Pérez", Role.ESTUDIANTE, UserStatus.ACTIVO, "hash1");
        User u2 = new User("jperez", "Otro Nombre", Role.DOCENTE, UserStatus.INACTIVO, "hash2");
 
        assertEquals(u1, u2); // la igualdad se basa en el login (clave de negocio)
    }
 
    @Test
    void usuarioInactivoNoDeberiaEstarActivo() {
        User user = new User("jperez", "Juan Pérez", Role.ESTUDIANTE, UserStatus.INACTIVO, "hash123");
 
        assertFalse(user.isActive());
    }
}
 