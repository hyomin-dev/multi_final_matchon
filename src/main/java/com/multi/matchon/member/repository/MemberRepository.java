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
            "LEFT JOIN FETCH m.positions p " +
            "LEFT JOIN FETCH m.team t " +
            "WHERE m.memberEmail = :email")
    Optional<Member> findForMypage(@Param("email") String email);

    // Optional<Member> findWithFetchJoinByEmail(@Param("email") String email);

    @Query("select m from Member m join fetch m.team where m.memberEmail =:email")
    Optional<Member> findByEmailWithTeam(@Param("email") String email);

}
