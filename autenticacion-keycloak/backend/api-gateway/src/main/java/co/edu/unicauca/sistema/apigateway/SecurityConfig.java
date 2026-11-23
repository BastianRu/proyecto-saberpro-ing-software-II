package co.edu.unicauca.sistema.apigateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Gateway security: this is where the JWT is validated and where the first role check happens.
 *
 * <p>Spring Cloud Gateway runs on WebFlux, so the reactive stack is required:
 * {@link EnableWebFluxSecurity}, {@link ServerHttpSecurity} and {@link SecurityWebFilterChain}
 * (the servlet-based {@code @EnableWebSecurity} does not apply here).</p>
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/a/hello_student", "/b/hello_student").hasRole("student")
                        .pathMatchers("/a/hello_coordinator", "/b/hello_coordinator").hasRole("coordinator")
                        .pathMatchers("/a/hello_company", "/b/hello_company").hasRole("company")
                        // No role needed, but a valid token is: unauthenticated callers get 401.
                        .pathMatchers("/a/hello_all", "/b/hello_all").authenticated()
                        .anyExchange().denyAll())
                // Validates the bearer token (signature, issuer, expiry) and builds the authentication.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter()))));
        return http.build();
    }

    /** Converter that exposes the Keycloak client roles as Spring Security authorities. */
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        converter.setPrincipalClaimName("preferred_username");
        return converter;
    }
}
