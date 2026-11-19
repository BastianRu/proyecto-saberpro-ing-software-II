package com.unicauca.lisw2t2g08.auditoria.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.unicauca.lisw2t2g08.auditoria.access.IAuditRepository;
import com.unicauca.lisw2t2g08.auditoria.access.SQLiteAuditRepository;
import com.unicauca.lisw2t2g08.auditoria.security.TokenVerifier;
import com.unicauca.lisw2t2g08.auditoria.service.AuditService;

/** Ensamblaje de dependencias del servicio. */
@Configuration
public class AuditConfig {

    @Bean(destroyMethod = "close")
    public Connection connection(@Value("${auditoria.db.url}") String url) throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS audit_records(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "event_id TEXT NOT NULL UNIQUE," +
                    "question_id TEXT NOT NULL," +
                    "event_type TEXT NOT NULL," +
                    "previous_state TEXT," +
                    "new_state TEXT," +
                    "username TEXT NOT NULL," +
                    "occurred_at TEXT NOT NULL" +
                    ");");
            st.execute("CREATE INDEX IF NOT EXISTS idx_audit_question ON audit_records(question_id);");
        }
        return connection;
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public IAuditRepository auditRepository(Connection connection) {
        return new SQLiteAuditRepository(connection);
    }

    @Bean
    public AuditService auditService(IAuditRepository repository) {
        return new AuditService(repository);
    }

    @Bean
    public TokenVerifier tokenVerifier(@Value("${auth.token.secret}") String secret, Clock clock) {
        return new TokenVerifier(secret, clock);
    }
}
