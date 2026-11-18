package com.example.gymclass.application.port.out;

import com.example.gymclass.domain.model.ClassSession;
import com.example.gymclass.domain.valueobject.SessionId;
import java.util.List;
import java.util.Optional;

/** Secondary (outbound) port required by the application to persist ClassSession aggregates. */
public interface ClassSessionRepositoryPort {
    /** Stores a complete class-session aggregate. */
    ClassSession save(ClassSession session);
    /** Finds a class session by identity. */
    Optional<ClassSession> findById(SessionId id);
    /** Returns all scheduled class sessions. */
    List<ClassSession> findAll();
}
