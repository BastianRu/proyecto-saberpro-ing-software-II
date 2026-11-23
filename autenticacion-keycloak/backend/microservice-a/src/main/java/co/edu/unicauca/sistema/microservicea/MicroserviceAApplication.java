package co.edu.unicauca.sistema.microservicea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of microservice-a (port 8082).
 *
 * <p>It is an OAuth2 <em>resource server</em>: it never issues tokens, it only validates the
 * JWT sent by the caller and authorizes each endpoint according to the caller's roles.</p>
 */
@SpringBootApplication
public class MicroserviceAApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroserviceAApplication.class, args);
    }
}
