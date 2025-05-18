package com.multi.matchon.member.repository;

import com.multi.matchon.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMemberEmail(String email);

    @Query("SELECT m FROM Member m " +
            "JOIN FETCH m.sportsType st " +
            "JOIN FETCH m.positions p " +
            "WHERE m.memberEmail = :email")
    Optional<Member> findWithFetchJoinByEmail(@Param("email") String email);
}
