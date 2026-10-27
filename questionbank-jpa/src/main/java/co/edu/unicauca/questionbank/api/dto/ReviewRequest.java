package co.edu.unicauca.questionbank.api.dto;

/**
 * Request body of the review operations.
 *
 * @param reviewerId reviewer identifier
 * @param reason     rejection reason (only for rejections)
 */
public record ReviewRequest(Long reviewerId, String reason) {
}
