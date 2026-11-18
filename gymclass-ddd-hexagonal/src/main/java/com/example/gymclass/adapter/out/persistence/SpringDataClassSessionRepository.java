package com.example.gymclass.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/** Spring Data repository used internally by the class-session persistence adapter. */
public interface SpringDataClassSessionRepository extends JpaRepository<ClassSessionJpaEntity, UUID> {}
