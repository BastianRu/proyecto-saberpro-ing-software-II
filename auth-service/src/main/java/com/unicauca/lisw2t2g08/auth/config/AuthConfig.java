package com.unicauca.lisw2t2g08.auth.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.unicauca.lisw2t2g08.auth.access.Argon2PasswordHasher;
import com.unicauca.lisw2t2g08.auth.access.IUserRepository;
import com.unicauca.lisw2t2g08.auth.access.SQLiteUserRepository;
import com.unicauca.lisw2t2g08.auth.domain.IPasswordHasher;
import com.unicauca.lisw2t2g08.auth.service.IUserService;
import com.unicauca.lisw2t2g08.auth.service.TokenSigner;
import com.unicauca.lisw2t2g08.auth.service.UserService;

// Ensamblaje de dependencias (lo que en el monolito hacia MainApp)

@Configuration
public class AuthConfig {

    @Bean(destroyMethod = "close")
    public Connection connection(@Value("${auth.db.url}") String url) throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        initializeSchema(connection);
        return connection;
    }

    @Bean
    public IUserRepository userRepository(Connection connection) {
        return new SQLiteUserRepository(connection);
    }

    @Bean
    public IPasswordHasher passwordHasher() {
        return new Argon2PasswordHasher();
    }

    @Bean
    public TokenSigner tokenSigner(@Value("${auth.token.secret}") String secret,
                                   @Value("${auth.token.ttl-minutes}") long ttlMinutes) {
        return new TokenSigner(secret, Duration.ofMinutes(ttlMinutes), Clock.systemDefaultZone());
    }

    @Bean
    public IUserService userService(IUserRepository repository, IPasswordHasher hasher, TokenSigner signer) {
        return new UserService(repository, hasher, signer);
    }

    private static void initializeSchema(Connection connection) throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS users(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "login TEXT NOT NULL UNIQUE," +
                    "full_name TEXT NOT NULL," +
                    "role TEXT NOT NULL," +
                    "status TEXT NOT NULL," +
                    "hashed_password TEXT NOT NULL" +
                    ");");
        }
    }
}
