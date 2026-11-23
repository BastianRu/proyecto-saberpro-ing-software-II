package co.edu.unicauca.sistema.microserviceb;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Four endpoints, one per role plus one that accepts any authenticated user.
 */
@RestController
public class HelloController {

    private static final String SERVICE = "microservice-b";

    /** Any authenticated user (no specific role required). */
    @GetMapping("/hello_all")
    public String helloAll() {
        return "Hello from ALL from " + SERVICE;
    }

    @GetMapping("/hello_student")
    @PreAuthorize("hasRole('student')")
    public String helloStudent() {
        return "Hello student from " + SERVICE;
    }

    @GetMapping("/hello_coordinator")
    @PreAuthorize("hasRole('coordinator')")
    public String helloCoordinator() {
        return "Hello coordinator from " + SERVICE;
    }

    @GetMapping("/hello_company")
    @PreAuthorize("hasRole('company')")
    public String helloCompany() {
        return "Hello company from " + SERVICE;
    }
}
