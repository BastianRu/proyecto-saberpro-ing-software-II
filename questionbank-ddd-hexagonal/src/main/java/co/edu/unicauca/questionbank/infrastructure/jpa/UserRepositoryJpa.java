package co.edu.unicauca.questionbank.infrastructure.jpa;

import co.edu.unicauca.questionbank.domain.model.User;
import co.edu.unicauca.questionbank.domain.repository.UserRepository;
import co.edu.unicauca.questionbank.infrastructure.jpa.mapper.CatalogMapper;
import co.edu.unicauca.questionbank.infrastructure.jpa.repository.SpringUserDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** JPA implementation of the {@link UserRepository} contract. */
@Repository
@Transactional(readOnly = true)
public class UserRepositoryJpa implements UserRepository {

    private final SpringUserDataRepository data;

    /** @param data Spring Data repository */
    public UserRepositoryJpa(SpringUserDataRepository data) {
        this.data = data;
    }

    @Override
    @Transactional
    public User save(User user) {
        return CatalogMapper.toDomain(data.save(CatalogMapper.toRecord(user)));
    }

    @Override
    public Optional<User> findById(Long id) {
        return data.findById(id).map(CatalogMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return data.findAll().stream().map(CatalogMapper::toDomain).toList();
    }

    @Override
    public boolean existsByUsername(String username) {
        return data.existsByUsername(username);
    }
}
