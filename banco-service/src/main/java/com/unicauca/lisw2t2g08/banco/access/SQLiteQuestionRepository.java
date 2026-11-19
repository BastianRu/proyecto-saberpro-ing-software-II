package com.unicauca.lisw2t2g08.banco.access;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.unicauca.lisw2t2g08.banco.domain.DifficultyLevel;
import com.unicauca.lisw2t2g08.banco.domain.Question;
import com.unicauca.lisw2t2g08.banco.domain.QuestionDistractors;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStates;

/**
 * Implementacion de IQuestionRepository usando JDBC para SQLite.
 *
 * Es la unica clase que sabe escribir SQL para preguntas. El historial de cambios de
 * estado ya no se guarda aqui: es responsabilidad del servicio de Auditoria. Las opciones de
 * respuesta se guardan en la tabla question_options, una fila por opcion.
 */
public class SQLiteQuestionRepository implements IQuestionRepository {

    private final Connection connection;

    public SQLiteQuestionRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public synchronized List<Question> findAll() {
        String sql = "SELECT * FROM questions ORDER BY id";
        List<Question> questions = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                questions.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener todas las preguntas", e);
        }
        return questions;
    }

    @Override
    public synchronized Optional<Question> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = "SELECT * FROM questions WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar la pregunta: " + id, e);
        }
    }

    @Override
    public synchronized boolean save(Question question) {
        if (question == null || findById(question.getId()).isPresent()) {
            return false;
        }
        String sql = "INSERT INTO questions (id, name, statement, correct_index, state,"
                + " context, justification, bibliography, competence, topic, subtopic, difficulty)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        inTransaction(() -> {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, question.getId());
                ps.setString(2, question.getName());
                ps.setString(3, question.getStatement());
                ps.setInt(4, question.getDistractors().getCorrectIndex());
                ps.setString(5, question.getState().getCode());
                bindContent(ps, 6, question);
                ps.executeUpdate();
            }
            insertOptions(question);
        }, "Error al guardar la pregunta: " + question.getId());
        return true;
    }

    @Override
    public synchronized boolean update(Question question) {
        if (question == null) {
            return false;
        }
        String sql = "UPDATE questions SET name = ?, statement = ?, correct_index = ?, state = ?,"
                + " context = ?, justification = ?, bibliography = ?, competence = ?,"
                + " topic = ?, subtopic = ?, difficulty = ? WHERE id = ?";
        boolean[] updated = {false};
        inTransaction(() -> {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, question.getName());
                ps.setString(2, question.getStatement());
                ps.setInt(3, question.getDistractors().getCorrectIndex());
                ps.setString(4, question.getState().getCode());
                bindContent(ps, 5, question);
                ps.setString(12, question.getId());
                updated[0] = ps.executeUpdate() > 0;
            }
            if (updated[0]) {
                deleteOptions(question.getId());
                insertOptions(question);
            }
        }, "Error al actualizar la pregunta: " + question.getId());
        return updated[0];
    }

    @Override
    public synchronized Map<String, Integer> countByState() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        String sql = "SELECT state, COUNT(*) AS total FROM questions GROUP BY state";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                counts.put(rs.getString("state"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar las preguntas por estado", e);
        }
        return counts;
    }

    // Asigna desde "first" los siete campos de contenido de RF-05
    private void bindContent(PreparedStatement ps, int first, Question question) throws SQLException {
        ps.setString(first, question.getContext());
        ps.setString(first + 1, question.getJustification());
        ps.setString(first + 2, question.getBibliography());
        ps.setString(first + 3, question.getCompetence());
        ps.setString(first + 4, question.getTopic());
        ps.setString(first + 5, question.getSubtopic());
        ps.setString(first + 6, question.getDifficulty() == null ? "" : question.getDifficulty().name());
    }

    private void insertOptions(Question question) throws SQLException {
        String sql = "INSERT INTO question_options (question_id, position, text) VALUES (?, ?, ?)";
        List<String> options = question.getDistractors().getOptions();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < options.size(); i++) {
                ps.setString(1, question.getId());
                ps.setInt(2, i);
                ps.setString(3, options.get(i));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void deleteOptions(String questionId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM question_options WHERE question_id = ?")) {
            ps.setString(1, questionId);
            ps.executeUpdate();
        }
    }

    private List<String> findOptions(String questionId) throws SQLException {
        String sql = "SELECT text FROM question_options WHERE question_id = ? ORDER BY position";
        List<String> options = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    options.add(rs.getString("text"));
                }
            }
        }
        return options;
    }

    // Convierte una fila del ResultSet (con sus opciones) en un objeto Question
    private Question mapRow(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String difficulty = rs.getString("difficulty");
        return new Question(
                id,
                rs.getString("name"),
                rs.getString("context"),
                rs.getString("statement"),
                new QuestionDistractors(findOptions(id), rs.getInt("correct_index")),
                rs.getString("justification"),
                rs.getString("bibliography"),
                rs.getString("competence"),
                rs.getString("topic"),
                rs.getString("subtopic"),
                (difficulty == null || difficulty.isBlank()) ? null : DifficultyLevel.valueOf(difficulty),
                QuestionStates.fromCode(rs.getString("state")));
    }

    // Ejecuta varias sentencias como una sola unidad: o se aplican todas o ninguna
    private void inTransaction(SqlWork work, String errorMessage) {
        try {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                work.run();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException e) {
            throw new RuntimeException(errorMessage, e);
        }
    }

    @FunctionalInterface
    private interface SqlWork {
        void run() throws SQLException;
    }
}
