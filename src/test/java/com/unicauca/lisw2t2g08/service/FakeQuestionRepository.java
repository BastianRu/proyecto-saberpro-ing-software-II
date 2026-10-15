package com.unicauca.lisw2t2g08.service;

import com.unicauca.lisw2t2g08.access.IQuestionRepository;
import com.unicauca.lisw2t2g08.domain.Question;
import com.unicauca.lisw2t2g08.domain.QuestionStateChange;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Doble de prueba del repositorio. Al depender el dominio de la abstraccion
 * IQuestionRepository (DIP), el servicio se puede probar sin la capa de acceso a
 * datos real.
 */
class FakeQuestionRepository implements IQuestionRepository {

    private final Map<String, Question> questions = new LinkedHashMap<>();
    private final List<QuestionStateChange> history = new ArrayList<>();

    @Override
    public List<Question> findAll() {
        return new ArrayList<>(questions.values());
    }

    @Override
    public Optional<Question> findById(String id) {
        return Optional.ofNullable(questions.get(id));
    }

    @Override
    public boolean save(Question question) {
        if (question == null || questions.containsKey(question.getId())) {
            return false;
        }
        questions.put(question.getId(), question);
        return true;
    }

    @Override
    public boolean update(Question question) {
        if (question == null || !questions.containsKey(question.getId())) {
            return false;
        }
        questions.put(question.getId(), question);
        return true;
    }

    @Override
    public void saveStateChange(QuestionStateChange change) {
        if (change != null) {
            history.add(change);
        }
    }

    @Override
    public List<QuestionStateChange> findStateHistory(String questionId) {
        List<QuestionStateChange> result = new ArrayList<>();
        for (QuestionStateChange change : history) {
            if (change.getQuestionId().equals(questionId)) {
                result.add(change);
            }
        }
        return result;
    }
}
