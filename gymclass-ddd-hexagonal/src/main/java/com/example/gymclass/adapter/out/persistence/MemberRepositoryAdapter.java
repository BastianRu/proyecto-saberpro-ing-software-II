package com.example.gymclass.adapter.out.persistence;

import com.example.gymclass.domain.model.Member;
import com.example.gymclass.application.port.out.MemberRepositoryPort;
import com.example.gymclass.domain.valueobject.MemberId;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/** Adapts the application MemberRepositoryPort to Spring Data JPA. */
@Repository
public class MemberRepositoryAdapter implements MemberRepositoryPort {
    private final SpringDataMemberRepository repository;
    /** Injects the Spring Data implementation. */
    public MemberRepositoryAdapter(SpringDataMemberRepository repository) { this.repository=repository; }
    @Override public Member save(Member member) { return repository.save(MemberJpaEntity.fromDomain(member)).toDomain(); }
    @Override public Optional<Member> findById(MemberId id) { return repository.findById(id.value()).map(MemberJpaEntity::toDomain); }
    @Override public boolean existsByEmail(String email) { return repository.existsByEmail(email); }
}
