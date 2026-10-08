package com.unicauca.lisw2t2g08.access;

import java.util.List;

import com.unicauca.lisw2t2g08.domain.Question;
import com.unicauca.lisw2t2g08.domain.QuestionDistractors;
import com.unicauca.lisw2t2g08.domain.QuestionState;

/**
 * Preguntas de ejemplo para poder ejercitar la interfaz grafica la primera vez
 * que se crea la base de datos.
 */
public final class QuestionSampleData {

    private QuestionSampleData() {
    }

    /**
     * Carga las preguntas de ejemplo solo si el repositorio esta vacio, para no
     * sobrescribir los cambios de estado guardados en ejecuciones anteriores.
     *
     * @param repository repositorio donde se cargan las preguntas.
     */
    public static void loadIfEmpty(IQuestionRepository repository) {
        if (!repository.findAll().isEmpty()) {
            return;
        }
        repository.save(new Question("P-001", "Pregunta sobre DDD",
                "¿Cual es el objetivo principal de DDD?",
                new QuestionDistractors(List.of(
                        "Disenar bases de datos",
                        "Modelar el dominio del negocio",
                        "Eliminar UML",
                        "Crear interfaces graficas"), 1),
                QuestionState.BORRADOR));

        repository.save(new Question("P-002", "Patron Observer",
                "¿Que problema resuelve el patron Observer?",
                new QuestionDistractors(List.of(
                        "Crear objetos sin exponer la logica de creacion",
                        "Notificar automaticamente a varios objetos un cambio de estado",
                        "Convertir una interfaz en otra",
                        "Restringir una clase a una unica instancia"), 1),
                QuestionState.BORRADOR));

        repository.save(new Question("P-003", "Arquitectura en capas",
                "¿Cual es la responsabilidad de la capa de acceso a datos?",
                new QuestionDistractors(List.of(
                        "Interactuar con el usuario final",
                        "Contener las reglas de negocio",
                        "Gestionar la persistencia de la informacion",
                        "Configurar el sistema operativo"), 2),
                QuestionState.PENDIENTE_REVISION));

        repository.save(new Question("P-004", "Principio SRP",
                "¿Que establece el principio de responsabilidad unica?",
                new QuestionDistractors(List.of(
                        "Una clase debe tener una sola razon para cambiar",
                        "Una clase debe heredar de una sola clase",
                        "Un metodo debe tener un solo parametro",
                        "Un paquete debe tener una sola clase"), 0),
                QuestionState.PENDIENTE_REVISION));

        repository.save(new Question("P-005", "Principio DIP",
                "¿De que deben depender los modulos de alto nivel?",
                new QuestionDistractors(List.of(
                        "De los modulos de bajo nivel",
                        "De abstracciones",
                        "Del framework de persistencia",
                        "De la interfaz grafica"), 1),
                QuestionState.BORRADOR));

        repository.save(new Question("P-006", "Pruebas unitarias",
                "¿Que caracteriza a una prueba unitaria?",
                new QuestionDistractors(List.of(
                        "Verifica el sistema completo desplegado",
                        "Requiere siempre una base de datos real",
                        "Verifica de forma aislada una unidad de codigo",
                        "Se ejecuta unicamente de forma manual"), 2),
                QuestionState.ELIMINADA));

        repository.save(new Question("P-007", "Micro patron MVC",
                "¿Que responsabilidad tiene el controlador en MVC?",
                new QuestionDistractors(List.of(
                        "Almacenar los datos en disco",
                        "Renderizar los componentes graficos",
                        "Coordinar la vista con el modelo",
                        "Definir el esquema de la base de datos"), 2),
                QuestionState.BORRADOR));

        repository.save(new Question("P-008", "Saber Pro",
                "¿Que competencia evalua el modulo de razonamiento cuantitativo?",
                new QuestionDistractors(List.of(
                        "La comprension de textos literarios",
                        "El uso de informacion cuantitativa para resolver problemas",
                        "El dominio de un segundo idioma",
                        "La redaccion de ensayos argumentativos"), 1),
                QuestionState.PENDIENTE_REVISION));
    }
}
