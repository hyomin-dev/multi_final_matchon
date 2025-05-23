package com.multi.matchon.member.repository;

import com.multi.matchon.member.domain.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 로그인 전용
    Optional<Member> findSimpleByMemberEmail(String email);

    // 마이페이지 전용 (positions, team fetch)
    @EntityGraph(attributePaths = {"positions", "team"})
    Optional<Member> findByMemberEmail(String email);


    // 팀 전용
    @Query("select m from Member m join fetch m.team where m.memberEmail =:email")
    Optional<Member> findByMemberEmailWithTeam(@Param("email") String email);


}
