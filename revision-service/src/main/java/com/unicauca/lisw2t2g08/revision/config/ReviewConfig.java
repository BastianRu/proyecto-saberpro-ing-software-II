package com.unicauca.lisw2t2g08.revision.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.unicauca.lisw2t2g08.revision.access.IReviewRepository;
import com.unicauca.lisw2t2g08.revision.access.SQLiteReviewRepository;
import com.unicauca.lisw2t2g08.revision.security.TokenVerifier;
import com.unicauca.lisw2t2g08.revision.service.IBancoClient;
import com.unicauca.lisw2t2g08.revision.service.RestBancoClient;
import com.unicauca.lisw2t2g08.revision.service.ReviewService;

/** Ensamblaje de dependencias del servicio. */
@Configuration
public class ReviewConfig {

    @Bean(destroyMethod = "close")
    public Connection connection(@Value("${revision.db.url}") String url) throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS reviews(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "question_id TEXT NOT NULL," +
                    "requested_by TEXT NOT NULL," +
                    "requested_at TEXT NOT NULL," +
                    "status TEXT NOT NULL," +
                    "reviewer TEXT," +
                    "started_at TEXT," +
                    "decision TEXT," +
                    "observations TEXT," +
                    "completed_at TEXT" +
                    ");");
            st.execute("CREATE INDEX IF NOT EXISTS idx_reviews_status ON reviews(status);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_reviews_question ON reviews(question_id);");
            // Eventos ya procesados: garantiza que una entrega duplicada no cree dos revisiones
            st.execute("CREATE TABLE IF NOT EXISTS processed_events(event_id TEXT PRIMARY KEY);");
        }
        return connection;
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public IReviewRepository reviewRepository(Connection connection) {
        return new SQLiteReviewRepository(connection);
    }

    @Bean
    public IBancoClient bancoClient(@Value("${revision.banco.url}") String url) {
        return new RestBancoClient(url);
    }

    @Bean
    public ReviewService reviewService(IReviewRepository repository, IBancoClient banco, Clock clock) {
        return new ReviewService(repository, banco, clock);
    }

    @Bean
    public TokenVerifier tokenVerifier(@Value("${auth.token.secret}") String secret, Clock clock) {
        return new TokenVerifier(secret, clock);
    }
}
