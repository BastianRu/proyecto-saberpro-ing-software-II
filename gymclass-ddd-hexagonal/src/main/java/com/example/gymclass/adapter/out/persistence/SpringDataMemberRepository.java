package com.example.gymclass.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/** Spring Data repository used internally by the member persistence adapter. */
public interface SpringDataMemberRepository extends JpaRepository<MemberJpaEntity, UUID> { boolean existsByEmail(String email); }
