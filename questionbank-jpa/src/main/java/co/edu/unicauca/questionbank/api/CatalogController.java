package co.edu.unicauca.questionbank.api;

import co.edu.unicauca.questionbank.api.dto.CatalogRequests.CompetencyRequest;
import co.edu.unicauca.questionbank.api.dto.CatalogRequests.TopicRequest;
import co.edu.unicauca.questionbank.api.dto.CatalogRequests.UserRequest;
import co.edu.unicauca.questionbank.application.CatalogService;
import co.edu.unicauca.questionbank.domain.model.Competency;
import co.edu.unicauca.questionbank.domain.model.Topic;
import co.edu.unicauca.questionbank.domain.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST endpoints for the reference data: users, topics and competencies. */
@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogService catalog;

    /** @param catalog catalog use cases */
    public CatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    /**
     * @param request user data
     * @return the created user
     */
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody UserRequest request) {
        return catalog.registerUser(request.username(), request.fullName(), request.email(), request.role());
    }

    /** @return every user */
    @GetMapping("/users")
    public List<User> users() {
        return catalog.users();
    }

    /**
     * @param request topic data
     * @return the created topic
     */
    @PostMapping("/topics")
    @ResponseStatus(HttpStatus.CREATED)
    public Topic createTopic(@RequestBody TopicRequest request) {
        return catalog.createTopic(request.name());
    }

    /** @return every topic */
    @GetMapping("/topics")
    public List<Topic> topics() {
        return catalog.topics();
    }

    /**
     * @param request competency data
     * @return the created competency
     */
    @PostMapping("/competencies")
    @ResponseStatus(HttpStatus.CREATED)
    public Competency createCompetency(@RequestBody CompetencyRequest request) {
        return catalog.createCompetency(request.code(), request.name());
    }

    /** @return every competency */
    @GetMapping("/competencies")
    public List<Competency> competencies() {
        return catalog.competencies();
    }
}
