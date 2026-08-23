package com.unicauca.lisw2.access;
 
import java.util.List;
import java.util.Optional;

import com.unicauca.lisw2.domain.User;


public interface IUserRepository {

  User save(User user);

  Optional<User> findByLogin(String login);

  List<User> findAll();

  boolean existByLogin(String login);

  void update(User user);
  
}
