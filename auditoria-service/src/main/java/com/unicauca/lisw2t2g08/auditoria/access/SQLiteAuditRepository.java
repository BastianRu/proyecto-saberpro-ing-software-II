package com.unicauca.lisw2t2g08.auditoria.access;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.unicauca.lisw2t2g08.auditoria.domain.AuditRecord;

/**
 * Implementación JDBC de IAuditRepository. La idempotencia la garantiza la base de datos:
 * event_id es UNIQUE e INSERT OR IGNORE descarta el duplicado de forma atómica.
 * Los métodos son synchronized porque los hilos del listener y los HTTP comparten la conexión.
 */
public class SQLiteAuditRepository implements IAuditRepository {

    private final Connection connection;

    public SQLiteAuditRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public synchronized boolean saveIfNew(AuditRecord record) {
        String sql = "INSERT OR IGNORE INTO audit_records (event_id, question_id, event_type,"
                + " previous_state, new_state, username, occurred_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, record.eventId());
            ps.setString(2, record.questionId());
            ps.setString(3, record.eventType());
            ps.setString(4, record.previousState());
            ps.setString(5, record.newState());
            ps.setString(6, record.username());
            ps.setString(7, record.occurredAt().toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el registro de auditoría " + record.eventId(), e);
        }
    }

    @Override
    public synchronized List<AuditRecord> findByQuestionId(String questionId) {
        // occurred_at está en ISO-8601 UTC, así que el orden alfabético es el cronológico
        String sql = "SELECT * FROM audit_records WHERE question_id = ? ORDER BY occurred_at, id";
        List<AuditRecord> records = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(new AuditRecord(rs.getString("event_id"), rs.getString("question_id"),
                            rs.getString("event_type"), rs.getString("previous_state"),
                            rs.getString("new_state"), rs.getString("username"),
                            Instant.parse(rs.getString("occurred_at"))));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar el historial de la pregunta " + questionId, e);
        }
        return records;
    }
}
