package co.edu.unicauca.questionbank.infrastructure.jpa;

import co.edu.unicauca.questionbank.domain.model.Topic;
import co.edu.unicauca.questionbank.domain.repository.TopicRepository;
import co.edu.unicauca.questionbank.infrastructure.jpa.mapper.CatalogMapper;
import co.edu.unicauca.questionbank.infrastructure.jpa.repository.SpringTopicDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** JPA implementation of the {@link TopicRepository} contract. */
@Repository
@Transactional(readOnly = true)
public class TopicRepositoryJpa implements TopicRepository {

    private final SpringTopicDataRepository data;

    /** @param data Spring Data repository */
    public TopicRepositoryJpa(SpringTopicDataRepository data) {
        this.data = data;
    }

    @Override
    @Transactional
    public Topic save(Topic topic) {
        return CatalogMapper.toDomain(data.save(CatalogMapper.toRecord(topic)));
    }

    @Override
    public Optional<Topic> findById(Long id) {
        return data.findById(id).map(CatalogMapper::toDomain);
    }

    @Override
    public List<Topic> findAll() {
        return data.findAll().stream().map(CatalogMapper::toDomain).toList();
    }
}
