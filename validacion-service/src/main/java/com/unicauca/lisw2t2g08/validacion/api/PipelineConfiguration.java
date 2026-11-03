package com.unicauca.lisw2t2g08.validacion.api;

import com.unicauca.lisw2t2g08.validacion.pipeline.QuestionPipeline;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Publica el pipeline como bean de Spring.
 *
 * El paquete {@code pipeline} se mantiene independiente del framework: la
 * tuberia se construye aqui con su constructor sin argumentos, que ya encadena
 * los siete filtros en el orden del monolito.
 */
@Configuration
public class PipelineConfiguration {

    @Bean
    public QuestionPipeline questionPipeline() {
        return new QuestionPipeline();
    }
}
