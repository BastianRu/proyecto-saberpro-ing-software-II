package co.edu.unicauca.questionbank.infrastructure.jpa.record;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA record of a multiple choice question (table {@code multiple_choice_questions}).
 * Its options are mapped as a one-to-many: the foreign key lives in
 * {@code answer_options.question_id} (owning side), and options are removed
 * with the question ({@code cascade = ALL, orphanRemoval = true}).
 */
@Entity
@Table(name = "multiple_choice_questions")
@DiscriminatorValue("MULTIPLE_CHOICE")
public class MultipleChoiceQuestionRecord extends QuestionRecord {

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<AnswerOptionRecord> options = new ArrayList<>();

    /** Creates an empty record; the mapper fills it. */
    public MultipleChoiceQuestionRecord() {
    }

    /** @return options ordered by position */
    public List<AnswerOptionRecord> getOptions() {
        return options;
    }

    /**
     * Adds an option and keeps both sides of the relationship consistent.
     *
     * @param option option record
     */
    public void addOption(AnswerOptionRecord option) {
        options.add(option);
        option.setQuestion(this);
    }

    /**
     * Removes an option; thanks to {@code orphanRemoval} its row is deleted.
     *
     * @param option option record
     */
    public void removeOption(AnswerOptionRecord option) {
        options.remove(option);
        option.setQuestion(null);
    }
}
