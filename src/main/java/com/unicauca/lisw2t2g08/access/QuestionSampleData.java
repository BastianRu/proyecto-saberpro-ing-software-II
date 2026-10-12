package com.unicauca.lisw2t2g08.access;

import java.util.List;

import com.unicauca.lisw2t2g08.domain.DifficultyLevel;
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
        repository.save(question("P-001", "Pregunta sobre DDD",
                "¿Cual es el objetivo principal de DDD?",
                List.of(
                        "Disenar bases de datos",
                        "Modelar el dominio del negocio",
                        "Eliminar UML",
                        "Crear interfaces graficas"), 1,
                "Diseño de software", "Diseño guiado por el dominio", "DDD", DifficultyLevel.MEDIA, QuestionState.BORRADOR));

        repository.save(question("P-002", "Patron Observer",
                "¿Que problema resuelve el patron Observer?",
                List.of(
                        "Crear objetos sin exponer la logica de creacion",
                        "Notificar automaticamente a varios objetos un cambio de estado",
                        "Convertir una interfaz en otra",
                        "Restringir una clase a una unica instancia"), 1,
                "Diseño de software", "Patrones de diseño", "Observer", DifficultyLevel.MEDIA, QuestionState.BORRADOR));

        repository.save(question("P-003", "Arquitectura en capas",
                "¿Cual es la responsabilidad de la capa de acceso a datos?",
                List.of(
                        "Interactuar con el usuario final",
                        "Contener las reglas de negocio",
                        "Gestionar la persistencia de la informacion",
                        "Configurar el sistema operativo"), 2,
                "Arquitectura de software", "Arquitectura en capas", "Capa de acceso a datos", DifficultyLevel.BAJA, QuestionState.PENDIENTE_REVISION));

        repository.save(question("P-004", "Principio SRP",
                "¿Que establece el principio de responsabilidad unica?",
                List.of(
                        "Una clase debe tener una sola razon para cambiar",
                        "Una clase debe heredar de una sola clase",
                        "Un metodo debe tener un solo parametro",
                        "Un paquete debe tener una sola clase"), 0,
                "Diseño de software", "Principios SOLID", "SRP", DifficultyLevel.BAJA, QuestionState.PENDIENTE_REVISION));

        repository.save(question("P-005", "Principio DIP",
                "¿De que deben depender los modulos de alto nivel?",
                List.of(
                        "De los modulos de bajo nivel",
                        "De abstracciones",
                        "Del framework de persistencia",
                        "De la interfaz grafica"), 1,
                "Diseño de software", "Principios SOLID", "DIP", DifficultyLevel.MEDIA, QuestionState.BORRADOR));

        repository.save(question("P-006", "Pruebas unitarias",
                "¿Que caracteriza a una prueba unitaria?",
                List.of(
                        "Verifica el sistema completo desplegado",
                        "Requiere siempre una base de datos real",
                        "Verifica de forma aislada una unidad de codigo",
                        "Se ejecuta unicamente de forma manual"), 2,
                "Calidad de software", "Pruebas", "Pruebas unitarias", DifficultyLevel.BAJA, QuestionState.ELIMINADA));

        repository.save(question("P-007", "Micro patron MVC",
                "¿Que responsabilidad tiene el controlador en MVC?",
                List.of(
                        "Almacenar los datos en disco",
                        "Renderizar los componentes graficos",
                        "Coordinar la vista con el modelo",
                        "Definir el esquema de la base de datos"), 2,
                "Arquitectura de software", "Patrones arquitectónicos", "MVC", DifficultyLevel.MEDIA, QuestionState.BORRADOR));

        repository.save(question("P-008", "Saber Pro",
                "¿Que competencia evalua el modulo de razonamiento cuantitativo?",
                List.of(
                        "La comprension de textos literarios",
                        "El uso de informacion cuantitativa para resolver problemas",
                        "El dominio de un segundo idioma",
                        "La redaccion de ensayos argumentativos"), 1,
                "Razonamiento cuantitativo", "Saber Pro", "Competencias genéricas", DifficultyLevel.ALTA, QuestionState.PENDIENTE_REVISION));
    }

    // RF-05: las preguntas de ejemplo llevan todos los campos de una pregunta completa
    private static Question question(String id, String name, String statement,
                                     List<String> options, int correctIndex,
                                     String competence, String topic, String subtopic,
                                     DifficultyLevel difficulty, QuestionState state) {
        return new Question(id, name,
                "Contexto de ejemplo para la pregunta " + id + ": " + name + ".",
                statement, new QuestionDistractors(options, correctIndex),
                "La opcion correcta es la que corresponde al concepto de " + name + ".",
                "Material del curso de Ingenieria de Software II.",
                competence, topic, subtopic, difficulty, state);
    }
}
