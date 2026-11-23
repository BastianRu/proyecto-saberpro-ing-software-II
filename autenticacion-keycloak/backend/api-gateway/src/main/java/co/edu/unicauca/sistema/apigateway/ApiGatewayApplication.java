package co.edu.unicauca.sistema.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the API Gateway (port 8081): the single door through which the desktop client
 * reaches microservice-a ({@code /a/**}) and microservice-b ({@code /b/**}).
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
