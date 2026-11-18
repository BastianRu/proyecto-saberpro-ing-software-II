package co.edu.unicauca.questionbank.application;

import co.edu.unicauca.questionbank.domain.exception.BusinessRuleException;
import co.edu.unicauca.questionbank.domain.model.Competency;
import co.edu.unicauca.questionbank.domain.model.Role;
import co.edu.unicauca.questionbank.domain.model.Topic;
import co.edu.unicauca.questionbank.domain.model.User;
import co.edu.unicauca.questionbank.domain.repository.CompetencyRepository;
import co.edu.unicauca.questionbank.domain.repository.TopicRepository;
import co.edu.unicauca.questionbank.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use cases for the reference data the questions depend on: users, topics and
 * competencies. It only depends on repository contracts.
 */
@Service
@Transactional
public class CatalogService {

    private final UserRepository users;
    private final TopicRepository topics;
    private final CompetencyRepository competencies;

    /**
     * Creates the service.
     *
     * @param users        user repository contract
     * @param topics       topic repository contract
     * @param competencies competency repository contract
     */
    public CatalogService(UserRepository users, TopicRepository topics, CompetencyRepository competencies) {
        this.users = users;
        this.topics = topics;
        this.competencies = competencies;
    }

    /**
     * Registers a user.
     *
     * @param username unique login name
     * @param fullName full name
     * @param email    email address
     * @param role     role in the system
     * @return the stored user
     * @throws BusinessRuleException if the username is already taken
     */
    public User registerUser(String username, String fullName, String email, Role role) {
        if (users.existsByUsername(username)) {
            throw new BusinessRuleException("Username already exists: " + username);
        }
        return users.save(new User(null, username, fullName, email, role));
    }

    /**
     * @param name topic name
     * @return the stored topic
     */
    public Topic createTopic(String name) {
        return topics.save(new Topic(null, name));
    }

    /**
     * @param code competency code
     * @param name competency name
     * @return the stored competency
     */
    public Competency createCompetency(String code, String name) {
        return competencies.save(new Competency(null, code, name));
    }

    /** @return every user */
    @Transactional(readOnly = true)
    public List<User> users() {
        return users.findAll();
    }

    /** @return every topic */
    @Transactional(readOnly = true)
    public List<Topic> topics() {
        return topics.findAll();
    }

    /** @return every competency */
    @Transactional(readOnly = true)
    public List<Competency> competencies() {
        return competencies.findAll();
    }
}
