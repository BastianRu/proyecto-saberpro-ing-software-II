package com.unicauca.lisw2t2g08.banco.access;

import java.util.List;

import com.unicauca.lisw2t2g08.banco.domain.DifficultyLevel;
import com.unicauca.lisw2t2g08.banco.domain.Question;
import com.unicauca.lisw2t2g08.banco.domain.QuestionDistractors;
import com.unicauca.lisw2t2g08.banco.domain.QuestionState;
import com.unicauca.lisw2t2g08.banco.domain.QuestionStates;

/**
 * Preguntas de ejemplo para demos. Se cargan solo si banco.sample-data.enabled=true y la base esta vacia.
 * Se insertan directo en el repositorio, sin publicar eventos: Revision y Auditoria no se enteran de ellas,
 * por eso no se usan en el flujo normal (una pregunta sembrada en Pendiente de revision no tendria revision).
 */
public final class QuestionSampleData {

    private QuestionSampleData() {
    }

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
                        "Crear interfaces graficas",
                        "Desplegar en la nube"), 1,
                "Diseño de software", "Diseño guiado por el dominio", "DDD", DifficultyLevel.MEDIA, QuestionStates.BORRADOR));

        repository.save(question("P-002", "Patron Observer",
                "¿Que problema resuelve el patron Observer?",
                List.of(
                        "Crear objetos sin exponer la logica de creacion",
                        "Notificar automaticamente a varios objetos un cambio de estado",
                        "Convertir una interfaz en otra",
                        "Restringir una clase a una unica instancia",
                        "Encapsular llamadas remotas"), 1,
                "Diseño de software", "Patrones de diseño", "Observer", DifficultyLevel.MEDIA, QuestionStates.BORRADOR));

        repository.save(question("P-003", "Arquitectura en capas",
                "¿Cual es la responsabilidad de la capa de acceso a datos?",
                List.of(
                        "Interactuar con el usuario final",
                        "Contener las reglas de negocio",
                        "Gestionar la persistencia de la informacion",
                        "Configurar el sistema operativo",
                        "Renderizar componentes visuales"), 2,
                "Arquitectura de software", "Arquitectura en capas", "Capa de acceso a datos", DifficultyLevel.BAJA, QuestionStates.PENDIENTE_REVISION));

        repository.save(question("P-004", "Principio SRP",
                "¿Que establece el principio de responsabilidad unica?",
                List.of(
                        "Una clase debe tener una sola razon para cambiar",
                        "Una clase debe heredar de una sola clase",
                        "Un metodo debe tener un solo parametro",
                        "Un paquete debe tener una sola clase",
                        "Un modulo debe tener un solo autor"), 0,
                "Diseño de software", "Principios SOLID", "SRP", DifficultyLevel.BAJA, QuestionStates.EN_REVISION));

        repository.save(question("P-005", "Principio DIP",
                "¿De que deben depender los modulos de alto nivel?",
                List.of(
                        "De los modulos de bajo nivel",
                        "De abstracciones",
                        "Del framework de persistencia",
                        "De la interfaz grafica",
                        "De la base de datos relacional"), 1,
                "Diseño de software", "Principios SOLID", "DIP", DifficultyLevel.MEDIA, QuestionStates.RECHAZADA));

        repository.save(question("P-006", "Pruebas unitarias",
                "¿Que caracteriza a una prueba unitaria?",
                List.of(
                        "Verifica el sistema completo desplegado",
                        "Requiere siempre una base de datos real",
                        "Verifica de forma aislada una unidad de codigo",
                        "Se ejecuta unicamente de forma manual",
                        "Prueba la red de comunicacion"), 2,
                "Calidad de software", "Pruebas", "Pruebas unitarias", DifficultyLevel.BAJA, QuestionStates.ARCHIVADA));

        repository.save(question("P-007", "Micro patron MVC",
                "¿Que responsabilidad tiene el controlador en MVC?",
                List.of(
                        "Almacenar los datos en disco",
                        "Renderizar los componentes graficos",
                        "Coordinar la vista con el modelo",
                        "Definir el esquema de la base de datos",
                        "Garantizar la seguridad criptografica"), 2,
                "Arquitectura de software", "Patrones arquitectónicos", "MVC", DifficultyLevel.MEDIA, QuestionStates.PUBLICADA));

        repository.save(question("P-008", "Saber Pro",
                "¿Que competencia evalua el modulo de razonamiento cuantitativo?",
                List.of(
                        "La comprension de textos literarios",
                        "El uso de informacion cuantitativa para resolver problemas",
                        "El dominio de un segundo idioma",
                        "La redaccion de ensayos argumentativos",
                        "El diseño de software orientado a objetos"), 1,
                "Razonamiento cuantitativo", "Saber Pro", "Competencias genéricas", DifficultyLevel.ALTA, QuestionStates.APROBADA));
    }

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
