package com.unicauca.lisw2t2g08.auth.service;

import java.util.List;
import java.util.Optional;

import com.unicauca.lisw2t2g08.auth.access.IUserRepository;
import com.unicauca.lisw2t2g08.auth.domain.AuthToken;
import com.unicauca.lisw2t2g08.auth.domain.IPasswordHasher;
import com.unicauca.lisw2t2g08.auth.domain.PasswordPolicy;
import com.unicauca.lisw2t2g08.auth.domain.Role;
import com.unicauca.lisw2t2g08.auth.domain.User;
import com.unicauca.lisw2t2g08.auth.domain.UserStatus;


 // Implementación de la lógica de negocio de usuarios.


public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final IPasswordHasher passwordHasher;
    private final TokenSigner tokenSigner;

    public UserService(IUserRepository userRepository, IPasswordHasher passwordHasher, TokenSigner tokenSigner) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenSigner = tokenSigner;
    }

    @Override
    public User register(String login, String fullName, Role role, String rawPassword) {
        if (login != null && login.contains("|")) {
            throw new IllegalArgumentException("El login no puede contener el carácter '|'");
        }
        if (userRepository.existByLogin(login)) {
            throw new UserAlreadyExistsException("Ya existe un usuario con el login: " + login);
        }

        List<String> passwordErrors = PasswordPolicy.validate(rawPassword);
        if (!passwordErrors.isEmpty()) {
            throw new InvalidPasswordException(passwordErrors);
        }

        String hashedPassword = passwordHasher.hash(rawPassword);
        User newUser = new User(login, fullName, role, UserStatus.ACTIVO, hashedPassword);
        return userRepository.save(newUser);
    }

    @Override
    public AuthToken login(String login, String rawPassword) {
        Optional<User> maybeUser = userRepository.findByLogin(login);

        // Mensaje genérico para no revelar si el problema fue el login o la contraseña
        if (maybeUser.isEmpty()) {
            throw new AuthenticationException("Usuario o contraseña incorrectos");
        }

        User user = maybeUser.get();

        if (!user.isActive()) {
            throw new AuthenticationException("El usuario está inactivo");
        }

        if (!passwordHasher.matches(rawPassword, user.getHashedPassword())) {
            throw new AuthenticationException("Usuario o contraseña incorrectos");
        }

        return tokenSigner.issue(user);
    }

    @Override
    public List<User> listUsers() {
        return userRepository.findAll();
    }
}
