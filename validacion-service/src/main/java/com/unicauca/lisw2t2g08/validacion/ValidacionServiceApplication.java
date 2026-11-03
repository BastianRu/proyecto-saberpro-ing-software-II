package com.unicauca.lisw2t2g08.validacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio de Validacion Estructural (HU03).
 *
 * El servicio expone como API REST el pipeline de filtros del monolito. No
 * tiene base de datos ni llama a otros servicios.
 */
@SpringBootApplication
public class ValidacionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValidacionServiceApplication.class, args);
    }
}
