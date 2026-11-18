package co.edu.unicauca.questionbank.api;

import co.edu.unicauca.questionbank.api.dto.CreateQuizRequest;
import co.edu.unicauca.questionbank.api.dto.QuizResponse;
import co.edu.unicauca.questionbank.application.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST endpoints for quizzes. */
@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizzes;

    /** @param quizzes quiz use cases */
    public QuizController(QuizService quizzes) {
        this.quizzes = quizzes;
    }

    /**
     * Creates a quiz with published questions.
     *
     * @param request quiz data
     * @return the created quiz
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuizResponse create(@RequestBody CreateQuizRequest request) {
        return QuizResponse.from(quizzes.createQuiz(request.title(), request.teacherId(), request.questionIds()));
    }

    /** @return every quiz */
    @GetMapping
    public List<QuizResponse> list() {
        return quizzes.findAll().stream().map(QuizResponse::from).toList();
    }

    /**
     * @param id quiz identifier
     * @return the quiz
     */
    @GetMapping("/{id}")
    public QuizResponse get(@PathVariable Long id) {
        return QuizResponse.from(quizzes.findById(id));
    }
}
