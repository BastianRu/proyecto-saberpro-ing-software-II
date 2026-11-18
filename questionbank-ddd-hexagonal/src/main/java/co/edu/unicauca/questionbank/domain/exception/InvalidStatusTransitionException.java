package co.edu.unicauca.questionbank.domain.exception;

import co.edu.unicauca.questionbank.domain.model.QuestionStatus;

/** Signals an attempt to move a question to a status its lifecycle does not allow. */
public class InvalidStatusTransitionException extends BusinessRuleException {

    /**
     * Creates the exception.
     *
     * @param from current status of the question
     * @param to   requested status
     */
    public InvalidStatusTransitionException(QuestionStatus from, QuestionStatus to) {
        super("Transition " + from + " -> " + to + " is not allowed");
    }
}
