package co.edu.unicauca.questionbank.application.fake;

import co.edu.unicauca.questionbank.domain.model.Competency;
import co.edu.unicauca.questionbank.domain.model.Question;
import co.edu.unicauca.questionbank.domain.model.QuestionStatus;
import co.edu.unicauca.questionbank.domain.model.Quiz;
import co.edu.unicauca.questionbank.domain.model.Topic;
import co.edu.unicauca.questionbank.domain.model.User;
import co.edu.unicauca.questionbank.domain.repository.CompetencyRepository;
import co.edu.unicauca.questionbank.domain.repository.QuestionRepository;
import co.edu.unicauca.questionbank.domain.repository.QuizRepository;
import co.edu.unicauca.questionbank.domain.repository.TopicRepository;
import co.edu.unicauca.questionbank.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * In-memory implementations of the repository contracts. They show that the
 * application services can be tested without JPA or a database.
 */
public final class InMemoryRepositories {

    private InMemoryRepositories() {
    }

    /** Generic map-based store. */
    private static class Store<T> {
        private final Map<Long, T> items = new LinkedHashMap<>();
        private final AtomicLong sequence = new AtomicLong();
        private final Function<T, Long> getId;
        private final java.util.function.BiConsumer<T, Long> setId;

        Store(Function<T, Long> getId, java.util.function.BiConsumer<T, Long> setId) {
            this.getId = getId;
            this.setId = setId;
        }

        T save(T item) {
            if (getId.apply(item) == null) {
                setId.accept(item, sequence.incrementAndGet());
            }
            items.put(getId.apply(item), item);
            return item;
        }

        Optional<T> findById(Long id) {
            return Optional.ofNullable(items.get(id));
        }

        List<T> findAll() {
            return new ArrayList<>(items.values());
        }

        List<T> filter(Predicate<T> predicate) {
            return items.values().stream().filter(predicate).toList();
        }

        void delete(Long id) {
            items.remove(id);
        }
    }

    /** In-memory {@link UserRepository}. */
    public static class Users implements UserRepository {
        private final Store<User> store = new Store<>(User::getId, User::setId);

        @Override
        public User save(User user) {
            return store.save(user);
        }

        @Override
        public Optional<User> findById(Long id) {
            return store.findById(id);
        }

        @Override
        public List<User> findAll() {
            return store.findAll();
        }

        @Override
        public boolean existsByUsername(String username) {
            return !store.filter(u -> u.getUsername().equals(username)).isEmpty();
        }
    }

    /** In-memory {@link TopicRepository}. */
    public static class Topics implements TopicRepository {
        private final Store<Topic> store = new Store<>(Topic::getId, Topic::setId);

        @Override
        public Topic save(Topic topic) {
            return store.save(topic);
        }

        @Override
        public Optional<Topic> findById(Long id) {
            return store.findById(id);
        }

        @Override
        public List<Topic> findAll() {
            return store.findAll();
        }
    }

    /** In-memory {@link CompetencyRepository}. */
    public static class Competencies implements CompetencyRepository {
        private final Store<Competency> store = new Store<>(Competency::getId, Competency::setId);

        @Override
        public Competency save(Competency competency) {
            return store.save(competency);
        }

        @Override
        public Optional<Competency> findById(Long id) {
            return store.findById(id);
        }

        @Override
        public List<Competency> findAll() {
            return store.findAll();
        }
    }

    /** In-memory {@link QuestionRepository}. */
    public static class Questions implements QuestionRepository {
        private final Store<Question> store = new Store<>(Question::getId, Question::setId);

        @Override
        public Question save(Question question) {
            return store.save(question);
        }

        @Override
        public Optional<Question> findById(Long id) {
            return store.findById(id);
        }

        @Override
        public List<Question> findAll() {
            return store.findAll();
        }

        @Override
        public List<Question> findByAuthor(Long authorId) {
            return store.filter(q -> q.getAuthorId().equals(authorId));
        }

        @Override
        public List<Question> findByStatus(QuestionStatus status) {
            return store.filter(q -> q.getStatus() == status);
        }

        @Override
        public List<Question> findPublishedByCompetency(Long competencyId) {
            return store.filter(q -> q.getStatus() == QuestionStatus.PUBLISHED
                    && q.getCompetencyIds().contains(competencyId));
        }

        @Override
        public void deleteById(Long id) {
            store.delete(id);
        }
    }

    /** In-memory {@link QuizRepository}. */
    public static class Quizzes implements QuizRepository {
        private final Store<Quiz> store = new Store<>(Quiz::getId, Quiz::setId);

        @Override
        public Quiz save(Quiz quiz) {
            return store.save(quiz);
        }

        @Override
        public Optional<Quiz> findById(Long id) {
            return store.findById(id);
        }

        @Override
        public List<Quiz> findAll() {
            return store.findAll();
        }
    }
}
