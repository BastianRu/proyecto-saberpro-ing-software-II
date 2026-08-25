package com.unicauca.lisw2.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.unicauca.lisw2.access.IUserRepository;
import com.unicauca.lisw2.domain.User;

/**
 * Implementación en memoria de IUserRepository, usada SOLO en pruebas.
 *
 * Gracias a que UserService depende de la INTERFAZ IUserRepository
 * (no de SQLiteUserRepository), podemos "engañarlo" con esta versión
 * falsa y probar toda la lógica de negocio sin una base de datos real.
 * Esto es la Inversión de Dependencias dando frutos concretos: testeable
 * sin infraestructura.
 */
public class FakeUserRepository implements IUserRepository {

    private final Map<String, User> storage = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public User save(User user) {
        user.setId(nextId++);
        storage.put(user.getLogin(), user);
        return user;
    }

    @Override
    public Optional<User> findByLogin(String login) {
        return Optional.ofNullable(storage.get(login));
    }

    @Override
    public List<User> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public boolean existByLogin(String login) {
        return storage.containsKey(login);
    }

    @Override
    public void update(User user) {
        storage.put(user.getLogin(), user);
    }
}