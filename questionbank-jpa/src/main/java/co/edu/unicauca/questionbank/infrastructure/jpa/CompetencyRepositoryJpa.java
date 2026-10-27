package co.edu.unicauca.questionbank.infrastructure.jpa;

import co.edu.unicauca.questionbank.domain.model.Competency;
import co.edu.unicauca.questionbank.domain.repository.CompetencyRepository;
import co.edu.unicauca.questionbank.infrastructure.jpa.mapper.CatalogMapper;
import co.edu.unicauca.questionbank.infrastructure.jpa.repository.SpringCompetencyDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** JPA implementation of the {@link CompetencyRepository} contract. */
@Repository
@Transactional(readOnly = true)
public class CompetencyRepositoryJpa implements CompetencyRepository {

    private final SpringCompetencyDataRepository data;

    /** @param data Spring Data repository */
    public CompetencyRepositoryJpa(SpringCompetencyDataRepository data) {
        this.data = data;
    }

    @Override
    @Transactional
    public Competency save(Competency competency) {
        return CatalogMapper.toDomain(data.save(CatalogMapper.toRecord(competency)));
    }

    @Override
    public Optional<Competency> findById(Long id) {
        return data.findById(id).map(CatalogMapper::toDomain);
    }

    @Override
    public List<Competency> findAll() {
        return data.findAll().stream().map(CatalogMapper::toDomain).toList();
    }
}
