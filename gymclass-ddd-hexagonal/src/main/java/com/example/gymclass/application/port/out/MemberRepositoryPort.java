package com.example.gymclass.application.port.out;

import com.example.gymclass.domain.model.Member;
import com.example.gymclass.domain.valueobject.MemberId;
import java.util.Optional;

/** Secondary (outbound) port required by the application to persist Member aggregates. */
public interface MemberRepositoryPort {
    /** Stores a member aggregate. */
    Member save(Member member);
    /** Finds a member by identity. */
    Optional<Member> findById(MemberId id);
    /** Checks whether an email address is already registered. */
    boolean existsByEmail(String email);
}
