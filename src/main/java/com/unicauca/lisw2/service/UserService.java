package com.unicauca.lisw2.service;
 
import com.unicauca.lisw2.access.IUserRepository;
import com.unicauca.lisw2.domain.IPasswordHasher;
import com.unicauca.lisw2.domain.PasswordPolicy;
import com.unicauca.lisw2.domain.Role;
import com.unicauca.lisw2.domain.User;
import com.unicauca.lisw2.domain.UserStatus;
 
import java.util.List;
import java.util.Optional;
 


// Logica de negocio de usuarios  -- Recibe un IUsereRepository y un IPasswordHasher en el constructor,
//  no depende de la implementacion concreta de estas interfaces, sino de la abstraccion (interface) de estas.
//  Esto permite cambiar la implementacion concreta sin afectar la logica de negocio.


public class UserService implements IUserService {

  private final IUserRepository userRepository;
  private final IPasswordHasher passwordHasher;


  public UserService(IUserRepository userRepository, IPasswordHasher passwordHasher) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
  }

  
}
