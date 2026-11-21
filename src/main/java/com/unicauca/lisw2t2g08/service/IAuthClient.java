package com.unicauca.lisw2t2g08.service;

import com.unicauca.lisw2t2g08.domain.Role;
import com.unicauca.lisw2t2g08.domain.Session;

// Acceso del cliente al microservicio de Usuarios y Autenticacion.
// La interfaz grafica depende de este contrato, no de como se hace la llamada.

public interface IAuthClient {

  //Registra un nuevo usuario en el servicio de autenticacion
  void register(String login, String fullName, Role role, String rawPassword);

  //Verifica credenciales y retorna la sesion con el token firmado
  Session login(String login, String rawPassword);

}
