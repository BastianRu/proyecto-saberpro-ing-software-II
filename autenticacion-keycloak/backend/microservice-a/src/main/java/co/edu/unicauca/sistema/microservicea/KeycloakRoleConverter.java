package co.edu.unicauca.sistema.microservicea;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Converts the <em>client roles</em> that Keycloak places inside the access token into Spring
 * Security authorities.
 *
 * <p>Keycloak does not use the standard {@code scope} claim for roles. The roles of the client
 * {@value #CLIENT_ID} are nested as:</p>
 *
 * <pre>
 * "resource_access": { "sistema-desktop": { "roles": ["student"] } }
 * </pre>
 *
 * <p>Spring's default converter ignores that structure, so without this class every
 * {@code hasRole(..)} check would fail. Each role {@code r} becomes the authority {@code ROLE_r},
 * which is what {@code hasRole("r")} compares against.</p>
 */
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    /** Keycloak client whose roles are mapped. */
    public static final String CLIENT_ID = "sistema-desktop";

    private static final String RESOURCE_ACCESS_CLAIM = "resource_access";
    private static final String ROLES_KEY = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    /**
     * Extracts the roles of {@link #CLIENT_ID} from the token.
     *
     * @param jwt already validated access token
     * @return one {@code ROLE_<name>} authority per role; empty if the claim is missing or malformed
     */
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        if (!(jwt.getClaim(RESOURCE_ACCESS_CLAIM) instanceof Map<?, ?> resourceAccess)) {
            return List.of();
        }
        if (!(resourceAccess.get(CLIENT_ID) instanceof Map<?, ?> clientAccess)) {
            return List.of();
        }
        if (!(clientAccess.get(ROLES_KEY) instanceof Collection<?> roles)) {
            return List.of();
        }
        return roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .<GrantedAuthority>map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
                .toList();
    }
}
