package co.edu.unicauca.questionbank.config;

import co.edu.unicauca.questionbank.application.CatalogService;
import co.edu.unicauca.questionbank.application.QuestionService;
import co.edu.unicauca.questionbank.application.QuizService;
import co.edu.unicauca.questionbank.application.ReviewService;
import co.edu.unicauca.questionbank.domain.model.AnswerOption;
import co.edu.unicauca.questionbank.domain.model.CognitiveLevel;
import co.edu.unicauca.questionbank.domain.model.Competency;
import co.edu.unicauca.questionbank.domain.model.Difficulty;
import co.edu.unicauca.questionbank.domain.model.Question;
import co.edu.unicauca.questionbank.domain.model.QuestionMetadata;
import co.edu.unicauca.questionbank.domain.model.Role;
import co.edu.unicauca.questionbank.domain.model.Topic;
import co.edu.unicauca.questionbank.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Loads sample data through the use cases (never directly through JPA), so
 * the H2 console and the API have something to show right after startup.
 * Disabled with {@code questionbank.sample-data.enabled=false}.
 */
@Component
@ConditionalOnProperty(name = "questionbank.sample-data.enabled", havingValue = "true")
public class SampleDataLoader implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(SampleDataLoader.class);

    private final CatalogService catalog;
    private final QuestionService questions;
    private final ReviewService reviews;
    private final QuizService quizzes;

    /**
     * Creates the loader.
     *
     * @param catalog   catalog use cases
     * @param questions question use cases
     * @param reviews   review use cases
     * @param quizzes   quiz use cases
     */
    public SampleDataLoader(CatalogService catalog, QuestionService questions,
                            ReviewService reviews, QuizService quizzes) {
        this.catalog = catalog;
        this.questions = questions;
        this.reviews = reviews;
        this.quizzes = quizzes;
    }

    @Override
    public void run(String... args) {
        catalog.registerUser("admin", "Administrador del sistema", "admin@unicauca.edu.co", Role.ADMIN);
        User author = catalog.registerUser("ana.perez", "Ana Perez", "ana.perez@unicauca.edu.co", Role.AUTHOR);
        User reviewer = catalog.registerUser("luis.gomez", "Luis Gomez", "luis.gomez@unicauca.edu.co", Role.REVIEWER);
        User teacher = catalog.registerUser("maria.ruiz", "Maria Ruiz", "maria.ruiz@unicauca.edu.co", Role.TEACHER);
        catalog.registerUser("juan.diaz", "Juan Diaz", "juan.diaz@unicauca.edu.co", Role.STUDENT);

        Topic architecture = catalog.createTopic("Arquitectura de software");
        catalog.createTopic("Bases de datos");
        Competency design = catalog.createCompetency("DIS-SOL", "Diseno de soluciones");
        Competency analysis = catalog.createCompetency("ANA-PRO", "Analisis de problemas");

        Question layers = questions.createMultipleChoice(
                "Que patron organiza un sistema en presentacion, logica de negocio y acceso a datos?",
                Difficulty.LOW, author.getId(),
                List.of(AnswerOption.of("Microkernel", false), AnswerOption.of("Capas", true),
                        AnswerOption.of("Pipes and filters", false), AnswerOption.of("Broker", false),
                        AnswerOption.of("Peer to peer", false)),
                new QuestionMetadata(CognitiveLevel.REMEMBER, 60, "Pregunta introductoria", null),
                architecture.getId(), Set.of(design.getId()));
        publish(layers, reviewer);

        Question jpa = questions.createTrueFalse(
                "En una arquitectura por capas, las entidades de dominio deben llevar anotaciones JPA.",
                Difficulty.MEDIUM, author.getId(), false,
                new QuestionMetadata(CognitiveLevel.UNDERSTAND, 45, null, null),
                architecture.getId(), Set.of(design.getId(), analysis.getId()));
        publish(jpa, reviewer);

        Question pending = questions.createTrueFalse(
                "El patron Observer es un patron de comportamiento.", Difficulty.LOW, author.getId(), true,
                null, architecture.getId(), Set.of(design.getId()));
        questions.submitForReview(pending.getId());

        questions.createMultipleChoice("Cual estrategia de herencia JPA usa una sola tabla?", Difficulty.MEDIUM,
                author.getId(),
                List.of(AnswerOption.of("JOINED", false), AnswerOption.of("SINGLE_TABLE", true),
                        AnswerOption.of("TABLE_PER_CLASS", false), AnswerOption.of("MAPPED_SUPERCLASS", false),
                        AnswerOption.of("EMBEDDED", false)),
                null, null, null);

        quizzes.createQuiz("Simulacro de arquitectura", teacher.getId(), List.of(layers.getId(), jpa.getId()));
        LOG.info("Sample data loaded: 5 users, 2 topics, 2 competencies, 4 questions and 1 quiz");
    }

    private void publish(Question question, User reviewer) {
        questions.submitForReview(question.getId());
        reviews.startReview(question.getId(), reviewer.getId());
        reviews.approve(question.getId(), reviewer.getId());
        questions.publish(question.getId());
    }
}
