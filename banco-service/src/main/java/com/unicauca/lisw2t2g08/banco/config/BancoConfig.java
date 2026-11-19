package com.unicauca.lisw2t2g08.banco.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.unicauca.lisw2t2g08.banco.access.IQuestionRepository;
import com.unicauca.lisw2t2g08.banco.access.SQLiteQuestionRepository;
import com.unicauca.lisw2t2g08.banco.events.IEventPublisher;
import com.unicauca.lisw2t2g08.banco.events.RabbitEventPublisher;
import com.unicauca.lisw2t2g08.banco.pipeline.QuestionPipeline;
import com.unicauca.lisw2t2g08.banco.security.TokenVerifier;
import com.unicauca.lisw2t2g08.banco.service.IQuestionService;
import com.unicauca.lisw2t2g08.banco.service.QuestionService;

/** Ensamblaje de dependencias (lo que en el monolito hacía MainApp). */
@Configuration
public class BancoConfig {

    @Bean(destroyMethod = "close")
    public Connection connection(@Value("${banco.db.url}") String url) throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        initializeSchema(connection);
        return connection;
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public IQuestionRepository questionRepository(Connection connection) {
        return new SQLiteQuestionRepository(connection);
    }

    @Bean
    public QuestionPipeline questionPipeline() {
        return new QuestionPipeline();
    }

    @Bean
    public IEventPublisher eventPublisher(RabbitTemplate rabbitTemplate) {
        return new RabbitEventPublisher(rabbitTemplate);
    }

    @Bean
    public TokenVerifier tokenVerifier(@Value("${auth.token.secret}") String secret, Clock clock) {
        return new TokenVerifier(secret, clock);
    }

    @Bean
    public IQuestionService questionService(IQuestionRepository repository, QuestionPipeline pipeline,
                                            IEventPublisher events, Clock clock) {
        return new QuestionService(repository, pipeline, events, clock);
    }

    private static void initializeSchema(Connection connection) throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS questions(" +
                    "id TEXT PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "statement TEXT NOT NULL," +
                    "correct_index INTEGER NOT NULL," +
                    "state TEXT NOT NULL," +
                    "context TEXT NOT NULL DEFAULT ''," +
                    "justification TEXT NOT NULL DEFAULT ''," +
                    "bibliography TEXT NOT NULL DEFAULT ''," +
                    "competence TEXT NOT NULL DEFAULT ''," +
                    "topic TEXT NOT NULL DEFAULT ''," +
                    "subtopic TEXT NOT NULL DEFAULT ''," +
                    "difficulty TEXT NOT NULL DEFAULT ''" +
                    ");");
            st.execute("CREATE TABLE IF NOT EXISTS question_options(" +
                    "question_id TEXT NOT NULL REFERENCES questions(id)," +
                    "position INTEGER NOT NULL," +
                    "text TEXT NOT NULL," +
                    "PRIMARY KEY (question_id, position)" +
                    ");");
        }
    }
}
