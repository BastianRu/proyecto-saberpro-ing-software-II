package com.example.gymclass.adapter.out.persistence;

import com.example.gymclass.domain.model.ClassSession;
import com.example.gymclass.application.port.out.ClassSessionRepositoryPort;
import com.example.gymclass.domain.valueobject.SessionId;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/** Adapts the application ClassSessionRepositoryPort to Spring Data JPA. */
@Repository
public class ClassSessionRepositoryAdapter implements ClassSessionRepositoryPort {
    private final SpringDataClassSessionRepository repository;
    /** Injects the Spring Data implementation. */
    public ClassSessionRepositoryAdapter(SpringDataClassSessionRepository repository) { this.repository=repository; }
    @Override public ClassSession save(ClassSession session) { return repository.save(ClassSessionJpaEntity.fromDomain(session)).toDomain(); }
    @Override public Optional<ClassSession> findById(SessionId id) { return repository.findById(id.value()).map(ClassSessionJpaEntity::toDomain); }
    @Override public List<ClassSession> findAll() { return repository.findAll().stream().map(ClassSessionJpaEntity::toDomain).toList(); }
}
