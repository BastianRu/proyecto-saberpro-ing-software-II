package com.unicauca.lisw2.service;
 
import java.util.List;

import com.unicauca.lisw2.domain.Role;
import com.unicauca.lisw2.domain.User;


// la capa main depende de esta interfaz, no de la implementación


public interface IUserService {
  

  //Registra un nuevo usuario


  User register(String login, String fullName, Role role, String rawPassword);


  //Verifica credenciales y retorna el usuario si son validas

  User login(String login, String rawPassword);

  List<User> listUsers();



}
