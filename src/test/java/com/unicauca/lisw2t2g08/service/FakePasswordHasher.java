package com.unicauca.lisw2t2g08.service;

import com.unicauca.lisw2t2g08.domain.IPasswordHasher;

/**
 * Implementación FALSA (no segura) de IPasswordHasher, usada solo en
 * pruebas para no depender del algoritmo real (Argon2 es deliberadamente
 * lento, lo cual no queremos en cientos de pruebas unitarias) y para
 * aislar las pruebas de UserService de los detalles de "access".
 *
 * NUNCA debe usarse en producción — MainApp sigue usando Argon2PasswordHasher.
 */
public class FakePasswordHasher implements IPasswordHasher {

    private static final String PREFIX = "hashed:";

    @Override
    public String hash(String rawPassword) {
        return PREFIX + rawPassword;
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        return (PREFIX + rawPassword).equals(hashedPassword);
    }
}