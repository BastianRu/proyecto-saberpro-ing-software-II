package co.edu.unicauca.questionbank.infrastructure.jpa;

import co.edu.unicauca.questionbank.domain.model.Question;
import co.edu.unicauca.questionbank.domain.model.QuestionStatus;
import co.edu.unicauca.questionbank.domain.repository.QuestionRepository;
import co.edu.unicauca.questionbank.infrastructure.jpa.mapper.QuestionMapper;
import co.edu.unicauca.questionbank.infrastructure.jpa.record.CompetencyRecord;
import co.edu.unicauca.questionbank.infrastructure.jpa.record.QuestionRecord;
import co.edu.unicauca.questionbank.infrastructure.jpa.record.TopicRecord;
import co.edu.unicauca.questionbank.infrastructure.jpa.record.UserRecord;
import co.edu.unicauca.questionbank.infrastructure.jpa.repository.SpringCompetencyDataRepository;
import co.edu.unicauca.questionbank.infrastructure.jpa.repository.SpringQuestionDataRepository;
import co.edu.unicauca.questionbank.infrastructure.jpa.repository.SpringTopicDataRepository;
import co.edu.unicauca.questionbank.infrastructure.jpa.repository.SpringUserDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JPA implementation of the {@link QuestionRepository} contract. It resolves
 * the referenced records (author, topic, competencies), rejecting identifiers
 * that do not exist, and delegates the conversion to {@link QuestionMapper}.
 */
@Repository
@Transactional(readOnly = true)
public class QuestionRepositoryJpa implements QuestionRepository {

    private final SpringQuestionDataRepository questions;
    private final SpringUserDataRepository users;
    private final SpringTopicDataRepository topics;
    private final SpringCompetencyDataRepository competencies;

    /**
     * Creates the repository.
     *
     * @param questions    Spring Data question repository
     * @param users        Spring Data user repository
     * @param topics       Spring Data topic repository
     * @param competencies Spring Data competency repository
     */
    public QuestionRepositoryJpa(SpringQuestionDataRepository questions, SpringUserDataRepository users,
                                 SpringTopicDataRepository topics, SpringCompetencyDataRepository competencies) {
        this.questions = questions;
        this.users = users;
        this.topics = topics;
        this.competencies = competencies;
    }

    @Override
    @Transactional
    public Question save(Question question) {
        UserRecord author = users.findById(question.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("Author not found: " + question.getAuthorId()));
        TopicRecord topic = question.getTopicId() == null ? null : topics.findById(question.getTopicId())
                .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + question.getTopicId()));
        Set<CompetencyRecord> competencyRecords = question.getCompetencyIds().stream()
                .map(id -> competencies.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Competency not found: " + id)))
                .collect(Collectors.toSet());

        QuestionRecord record = question.getId() == null
                ? QuestionMapper.newRecord(question)
                : questions.findById(question.getId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + question.getId()));
        QuestionMapper.copyToRecord(question, record, author, topic, competencyRecords);
        return QuestionMapper.toDomain(questions.save(record));
    }

    @Override
    public Optional<Question> findById(Long id) {
        return questions.findById(id).map(QuestionMapper::toDomain);
    }

    @Override
    public List<Question> findAll() {
        return toDomain(questions.findAll());
    }

    @Override
    public List<Question> findByAuthor(Long authorId) {
        return toDomain(questions.findByAuthor_IdOrderByIdAsc(authorId));
    }

    @Override
    public List<Question> findByStatus(QuestionStatus status) {
        return toDomain(questions.findByStatusOrderByIdAsc(status));
    }

    @Override
    public List<Question> findPublishedByCompetency(Long competencyId) {
        return toDomain(questions.findByCompetencyAndStatus(competencyId, QuestionStatus.PUBLISHED));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        questions.deleteById(id);
    }

    private static List<Question> toDomain(List<QuestionRecord> records) {
        return records.stream().map(QuestionMapper::toDomain).toList();
    }
}
