package co.edu.unicauca.sistema.apigateway;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Maps the Keycloak client roles ({@code resource_access.sistema-desktop.roles}) to
 * {@code ROLE_<name>} authorities so that {@code hasRole(..)} works in the gateway.
 *
 * <p>Same contract as the converter of the microservices; each module keeps its own copy to stay
 * independently deployable.</p>
 */
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    /** Keycloak client whose roles are mapped. */
    public static final String CLIENT_ID = "sistema-desktop";

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        if (!(jwt.getClaim("resource_access") instanceof Map<?, ?> resourceAccess)) {
            return List.of();
        }
        if (!(resourceAccess.get(CLIENT_ID) instanceof Map<?, ?> clientAccess)) {
            return List.of();
        }
        if (!(clientAccess.get("roles") instanceof Collection<?> roles)) {
            return List.of();
        }
        return roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .<GrantedAuthority>map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }
}
