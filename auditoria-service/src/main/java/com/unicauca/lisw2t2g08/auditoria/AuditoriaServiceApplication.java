package com.unicauca.lisw2t2g08.auditoria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Microservicio de Auditoría e Historial (puerto 8084)

@SpringBootApplication
public class AuditoriaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditoriaServiceApplication.class, args);
    }
}
