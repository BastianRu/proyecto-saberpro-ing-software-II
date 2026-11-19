package com.unicauca.lisw2t2g08.auth.access;

import java.util.List;
import java.util.Optional;

import com.unicauca.lisw2t2g08.auth.domain.User;

public interface IUserRepository {

  User save(User user);

  Optional<User> findByLogin(String login);

  boolean existByLogin(String login);

  List<User> findAll();

}
