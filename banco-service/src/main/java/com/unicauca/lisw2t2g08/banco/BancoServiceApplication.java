package com.unicauca.lisw2t2g08.banco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Microservicio del Banco de Preguntas con validación estructural embebida (puerto 8082)

@SpringBootApplication
public class BancoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BancoServiceApplication.class, args);
    }
}
