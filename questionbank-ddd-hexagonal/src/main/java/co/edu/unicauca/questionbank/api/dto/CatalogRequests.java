package co.edu.unicauca.questionbank.api.dto;

import co.edu.unicauca.questionbank.domain.model.Role;

/** Request bodies of the catalog endpoints. */
public final class CatalogRequests {

    private CatalogRequests() {
    }

    /**
     * New user.
     *
     * @param username login name
     * @param fullName full name
     * @param email    email
     * @param role     role
     */
    public record UserRequest(String username, String fullName, String email, Role role) {
    }

    /**
     * New topic.
     *
     * @param name topic name
     */
    public record TopicRequest(String name) {
    }

    /**
     * New competency.
     *
     * @param code unique code
     * @param name name
     */
    public record CompetencyRequest(String code, String name) {
    }
}
