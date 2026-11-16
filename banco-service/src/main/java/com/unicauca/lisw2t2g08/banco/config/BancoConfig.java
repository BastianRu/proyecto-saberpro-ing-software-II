package com.unicauca.lisw2t2g08.banco.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.unicauca.lisw2t2g08.banco.access.IQuestionRepository;
import com.unicauca.lisw2t2g08.banco.access.QuestionSampleData;
import com.unicauca.lisw2t2g08.banco.access.SQLiteQuestionRepository;
import com.unicauca.lisw2t2g08.banco.client.IValidationClient;
import com.unicauca.lisw2t2g08.banco.client.RestValidationClient;
import com.unicauca.lisw2t2g08.banco.messaging.IQuestionEventPublisher;
import com.unicauca.lisw2t2g08.banco.messaging.RabbitMQQuestionEventPublisher;
import com.unicauca.lisw2t2g08.banco.security.TokenVerifier;
import com.unicauca.lisw2t2g08.banco.service.IQuestionService;
import com.unicauca.lisw2t2g08.banco.service.QuestionService;

/**
 * Configuracion y ensamblaje de dependencias para el microservicio banco-service.
 */
@Configuration
public class BancoConfig {

    @Bean(destroyMethod = "close")
    public Connection connection(@Value("${banco.db.url}") String url) throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        initializeSchema(connection);
        return connection;
    }

    @Bean
    public IQuestionRepository questionRepository(Connection connection) {
        SQLiteQuestionRepository repository = new SQLiteQuestionRepository(connection);
        QuestionSampleData.loadIfEmpty(repository);
        return repository;
    }

    @Bean
    public IValidationClient validationClient(@Value("${banco.validation.url}") String validationUrl) {
        return new RestValidationClient(validationUrl);
    }

    @Bean
    public TokenVerifier tokenVerifier(@Value("${banco.token.secret}") String secret) {
        return new TokenVerifier(secret);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    @Bean
    public TopicExchange questionsExchange(@Value("${banco.rabbitmq.exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public IQuestionEventPublisher eventPublisher(RabbitTemplate rabbitTemplate,
                                                 @Value("${banco.rabbitmq.exchange}") String exchangeName) {
        return new RabbitMQQuestionEventPublisher(rabbitTemplate, exchangeName);
    }

    @Bean
    public IQuestionService questionService(IQuestionRepository repository,
                                           IValidationClient validationClient,
                                           IQuestionEventPublisher eventPublisher) {
        return new QuestionService(repository, validationClient, eventPublisher);
    }

    private static void initializeSchema(Connection connection) throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS questions (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "statement TEXT NOT NULL, " +
                    "context TEXT, " +
                    "correct_index INTEGER NOT NULL, " +
                    "state TEXT NOT NULL, " +
                    "justification TEXT, " +
                    "bibliography TEXT, " +
                    "competence TEXT, " +
                    "topic TEXT, " +
                    "subtopic TEXT, " +
                    "difficulty TEXT" +
                    ");");

            st.execute("CREATE TABLE IF NOT EXISTS question_options (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "question_id TEXT NOT NULL, " +
                    "position INTEGER NOT NULL, " +
                    "text TEXT NOT NULL, " +
                    "FOREIGN KEY(question_id) REFERENCES questions(id)" +
                    ");");

            st.execute("CREATE TABLE IF NOT EXISTS question_state_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "question_id TEXT NOT NULL, " +
                    "from_state TEXT, " +
                    "to_state TEXT NOT NULL, " +
                    "username TEXT NOT NULL, " +
                    "changed_at TEXT NOT NULL, " +
                    "FOREIGN KEY(question_id) REFERENCES questions(id)" +
                    ");");
        }
    }
}
