package com.unicauca.lisw2.access;
 
import com.unicauca.lisw2.domain.IPasswordHasher;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;


// Implementacion para IPasswordHasher usando la libreria de Argon2.



public class Argon2PasswordHasher implements IPasswordHasher {


  private final Argon2 argon2  = Argon2Factory.create();

  //parametros recomendados

  private static final int ITERATIONS = 2;
  private static final int MEMORY = 65536; // 64MB
  private static final int PARALLELISM = 1;
  


  @Override
  public String hash(String rawPassword){
    char[] passwordChars = rawPassword.toCharArray();

    try {
        return argon2.hash(ITERATIONS, MEMORY, PARALLELISM, passwordChars);
    } finally  {
      argon2.wipeArray(passwordChars); // borra la constraña de la memoria
    }
  }
@Override
    public boolean matches(String rawPassword, String hashedPassword) {
        char[] passwordChars = rawPassword.toCharArray();
        try {
            return argon2.verify(hashedPassword, passwordChars);
        } finally {
            argon2.wipeArray(passwordChars);
        }


      }
}
