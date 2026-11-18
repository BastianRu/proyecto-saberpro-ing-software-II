package co.edu.unicauca.questionbank.api.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JSON body returned when a request fails.
 *
 * @param status    HTTP status code
 * @param error     short error name
 * @param message   description of the problem
 * @param details   individual errors (for example, every broken content rule)
 * @param timestamp moment of the error
 */
public record ErrorResponse(int status, String error, String message, List<String> details,
                            LocalDateTime timestamp) {
}
