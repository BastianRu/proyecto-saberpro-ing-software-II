package com.unicauca.lisw2t2g08.banco.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.unicauca.lisw2t2g08.banco.access.IQuestionRepository;
import com.unicauca.lisw2t2g08.banco.domain.Question;

/** Repositorio en memoria, solo para pruebas. */
class FakeQuestionRepository implements IQuestionRepository {

    private final Map<String, Question> storage = new LinkedHashMap<>();

    @Override
    public List<Question> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public Optional<Question> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public boolean save(Question question) {
        return storage.putIfAbsent(question.getId(), question) == null;
    }

    @Override
    public boolean update(Question question) {
        return storage.replace(question.getId(), question) != null;
    }

    @Override
    public Map<String, Integer> countByState() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        storage.values().forEach(q -> counts.merge(q.getState().getCode(), 1, Integer::sum));
        return counts;
    }
}
