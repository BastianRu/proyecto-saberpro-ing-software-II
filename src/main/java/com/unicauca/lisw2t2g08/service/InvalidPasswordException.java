package com.unicauca.lisw2t2g08.service;
import java.util.List;
// Se lanza cuando la contraseña no cumple con la política de seguridad.


public class InvalidPasswordException extends RuntimeException {
  

  private final List<String> errors;


  public InvalidPasswordException(List<String> errors) {
    super("La contraseña no cumple con la política de seguridad: " + String.join(", ", errors));
    this.errors = errors;
  }

  public List<String> getErrors() {
    return errors;
  }


  
}
