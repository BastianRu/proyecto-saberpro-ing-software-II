package com.unicauca.lisw2t2g08.auth.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.unicauca.lisw2t2g08.auth.access.IUserRepository;
import com.unicauca.lisw2t2g08.auth.domain.User;

// Repositorio en memoria, solo para pruebas
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
    public boolean existByLogin(String login) {
        return storage.containsKey(login);
    }

    @Override
    public List<User> findAll() {
        return List.copyOf(storage.values());
    }
}
