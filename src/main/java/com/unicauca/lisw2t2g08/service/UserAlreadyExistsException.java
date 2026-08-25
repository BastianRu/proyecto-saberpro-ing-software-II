package com.unicauca.lisw2t2g08.service;


// Se lanza cuando se intenta registrar un usuario con un login que ya existe en la base de datos.


public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
 