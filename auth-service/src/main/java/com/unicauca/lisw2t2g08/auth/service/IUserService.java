package com.unicauca.lisw2t2g08.auth.service;

import java.util.List;

import com.unicauca.lisw2t2g08.auth.domain.AuthToken;
import com.unicauca.lisw2t2g08.auth.domain.Role;
import com.unicauca.lisw2t2g08.auth.domain.User;

public interface IUserService {

  //Registra un nuevo usuario
  User register(String login, String fullName, Role role, String rawPassword);

  //Verifica credenciales y retorna un token firmado si son validas
  AuthToken login(String login, String rawPassword);

  List<User> listUsers();

}
