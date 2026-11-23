package co.edu.unicauca.sistema.microserviceb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of microservice-b (port 8083).
 *
 * <p>It is an OAuth2 <em>resource server</em>: it never issues tokens, it only validates the
 * JWT sent by the caller and authorizes each endpoint according to the caller's roles.</p>
 */
@SpringBootApplication
public class MicroserviceBApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroserviceBApplication.class, args);
    }
}
