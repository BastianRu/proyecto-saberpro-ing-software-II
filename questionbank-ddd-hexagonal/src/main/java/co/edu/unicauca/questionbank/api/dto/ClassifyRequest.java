package co.edu.unicauca.questionbank.api.dto;

import java.util.Set;

/**
 * Request body to classify a question.
 *
 * @param topicId       topic identifier
 * @param competencyIds competency identifiers
 */
public record ClassifyRequest(Long topicId, Set<Long> competencyIds) {
}
