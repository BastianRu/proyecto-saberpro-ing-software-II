package co.edu.unicauca.sistema.microservicea;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Resource-server security configuration.
 *
 * <p>Authorization is enforced twice (defense in depth): by URL here, and by method with
 * {@code @PreAuthorize} in {@link HelloController}. The service validates the JWT again even though
 * the API Gateway already did, so it stays safe if it is reached without going through the gateway.</p>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Stateless bearer-token API: no session and no cookies, hence nothing for CSRF to protect.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/hello_student").hasRole("student")
                        .requestMatchers("/hello_coordinator").hasRole("coordinator")
                        .requestMatchers("/hello_company").hasRole("company")
                        // hello_all needs no specific role, but the caller must still be authenticated.
                        .requestMatchers("/hello_all").authenticated()
                        .anyRequest().denyAll())
                // Missing/invalid token -> 401; valid token without the role -> 403.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    /** Builds the converter that turns a validated JWT into an authentication with Keycloak roles. */
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        converter.setPrincipalClaimName("preferred_username");
        return converter;
    }
}
