package com.unicauca.lisw2t2g08.banco.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Exige "Authorization: Bearer <token>" en todas las peticiones. Si el token es válido deja
 * al usuario como atributo de la petición; si no, responde 401. La autorización por rol
 * se profundiza en la iteración 3.
 */
@Component
public class BearerAuthFilter extends OncePerRequestFilter {

    public static final String USER_ATTRIBUTE = "authenticatedUser";
    private static final String PREFIX = "Bearer ";

    private final TokenVerifier verifier;

    public BearerAuthFilter(TokenVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        Optional<AuthenticatedUser> user = (header != null && header.startsWith(PREFIX))
                ? verifier.verify(header.substring(PREFIX.length()).trim())
                : Optional.empty();
        if (user.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getOutputStream().write(
                    "{\"message\":\"Token ausente, inválido o vencido\",\"errors\":[]}"
                            .getBytes(StandardCharsets.UTF_8));
            return;
        }
        request.setAttribute(USER_ATTRIBUTE, user.get());
        chain.doFilter(request, response);
    }
}
