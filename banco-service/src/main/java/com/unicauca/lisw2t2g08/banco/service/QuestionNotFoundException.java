package com.unicauca.lisw2t2g08.banco.service;

/** Se lanza cuando se opera sobre una pregunta que no existe. La API responde 404. */
public class QuestionNotFoundException extends RuntimeException {
    public QuestionNotFoundException(String id) {
        super("La pregunta no existe en el banco: " + id);
    }
}
