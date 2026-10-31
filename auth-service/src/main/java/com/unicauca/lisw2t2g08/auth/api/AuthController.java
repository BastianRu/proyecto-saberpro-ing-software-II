package com.unicauca.lisw2t2g08.auth.api;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.unicauca.lisw2t2g08.auth.domain.AuthToken;
import com.unicauca.lisw2t2g08.auth.domain.Role;
import com.unicauca.lisw2t2g08.auth.domain.User;
import com.unicauca.lisw2t2g08.auth.domain.UserStatus;
import com.unicauca.lisw2t2g08.auth.service.IUserService;
import com.unicauca.lisw2t2g08.auth.service.TokenSigner;


// Punto de entrada REST. Solo traduce HTTP <-> IUserService; no tiene logica de negocio.

@RestController
public class AuthController {

    public record RegisterRequest(String login, String fullName, Role role, String password) { }

    public record LoginRequest(String login, String password) { }

    // Nunca expone el hash de la contraseña
    public record UserResponse(Long id, String login, String fullName, Role role, UserStatus status) {
        static UserResponse of(User user) {
            return new UserResponse(user.getId(), user.getLogin(), user.getFullName(),
                    user.getRole(), user.getStatus());
        }
    }

    // 'token' es la cadena lista para usar como "Authorization: Bearer <token>"
    public record LoginResponse(String token, String subject, Role role, LocalDateTime expiresAt) { }

    private final IUserService userService;
    private final TokenSigner tokenSigner;

    public AuthController(IUserService userService, TokenSigner tokenSigner) {
        this.userService = userService;
        this.tokenSigner = tokenSigner;
    }

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody RegisterRequest request) {
        return UserResponse.of(userService.register(
                request.login(), request.fullName(), request.role(), request.password()));
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        AuthToken token = userService.login(request.login(), request.password());
        return new LoginResponse(tokenSigner.compact(token), token.getSubject(),
                token.getRole(), token.getExpiresAt());
    }

    @GetMapping("/usuarios")
    public List<UserResponse> listUsers() {
        return userService.listUsers().stream().map(UserResponse::of).toList();
    }
}
