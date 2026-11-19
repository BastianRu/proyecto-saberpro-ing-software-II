package com.unicauca.lisw2t2g08.revision.access;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.unicauca.lisw2t2g08.revision.domain.Review;
import com.unicauca.lisw2t2g08.revision.domain.ReviewDecision;
import com.unicauca.lisw2t2g08.revision.domain.ReviewStatus;

/** Implementación JDBC para SQLite. Métodos synchronized: los hilos del listener y los HTTP comparten la conexión. */
public class SQLiteReviewRepository implements IReviewRepository {

    private final Connection connection;

    public SQLiteReviewRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public synchronized boolean createFromEvent(String eventId, Review review) {
        try {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT OR IGNORE INTO processed_events (event_id) VALUES (?)")) {
                    ps.setString(1, eventId);
                    if (ps.executeUpdate() == 0) {
                        connection.rollback();
                        return false; // ya procesado
                    }
                }
                insert(review);
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear la revisión de la pregunta " + review.getQuestionId(), e);
        }
    }

    private void insert(Review review) throws SQLException {
        String sql = "INSERT INTO reviews (question_id, requested_by, requested_at, status)"
                + " VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, review.getQuestionId());
            ps.setString(2, review.getRequestedBy());
            ps.setString(3, review.getRequestedAt().toString());
            ps.setString(4, review.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    review.setId(keys.getLong(1));
                }
            }
        }
    }

    @Override
    public synchronized Optional<Review> findById(long id) {
        List<Review> found = query("SELECT * FROM reviews WHERE id = ?", String.valueOf(id));
        return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    @Override
    public synchronized List<Review> findByStatus(ReviewStatus status) {
        return status == null
                ? query("SELECT * FROM reviews ORDER BY requested_at, id")
                : query("SELECT * FROM reviews WHERE status = ? ORDER BY requested_at, id", status.name());
    }

    @Override
    public synchronized List<Review> findByQuestionId(String questionId) {
        return query("SELECT * FROM reviews WHERE question_id = ? ORDER BY requested_at, id", questionId);
    }

    @Override
    public synchronized void update(Review review) {
        String sql = "UPDATE reviews SET status = ?, reviewer = ?, started_at = ?, decision = ?,"
                + " observations = ?, completed_at = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, review.getStatus().name());
            ps.setString(2, review.getReviewer());
            ps.setString(3, review.getStartedAt() == null ? null : review.getStartedAt().toString());
            ps.setString(4, review.getDecision() == null ? null : review.getDecision().name());
            ps.setString(5, review.getObservations());
            ps.setString(6, review.getCompletedAt() == null ? null : review.getCompletedAt().toString());
            ps.setLong(7, review.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la revisión " + review.getId(), e);
        }
    }

    @Override
    public synchronized int cancelPendingByQuestion(String questionId, Instant now) {
        String sql = "UPDATE reviews SET status = ?, completed_at = ? WHERE question_id = ? AND status = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, ReviewStatus.CANCELADA.name());
            ps.setString(2, now.toString());
            ps.setString(3, questionId);
            ps.setString(4, ReviewStatus.PENDIENTE.name());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al cancelar las revisiones de la pregunta " + questionId, e);
        }
    }

    private List<Review> query(String sql, String... params) {
        List<Review> reviews = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar revisiones", e);
        }
        return reviews;
    }

    private static Review map(ResultSet rs) throws SQLException {
        return new Review(rs.getLong("id"), rs.getString("question_id"), rs.getString("requested_by"),
                Instant.parse(rs.getString("requested_at")), ReviewStatus.valueOf(rs.getString("status")),
                rs.getString("reviewer"), instant(rs.getString("started_at")),
                rs.getString("decision") == null ? null : ReviewDecision.valueOf(rs.getString("decision")),
                rs.getString("observations"), instant(rs.getString("completed_at")));
    }

    private static Instant instant(String value) {
        return value == null ? null : Instant.parse(value);
    }
}
