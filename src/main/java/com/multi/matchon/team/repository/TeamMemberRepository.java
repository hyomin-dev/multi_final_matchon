package com.multi.matchon.team.repository;

import com.multi.matchon.team.domain.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    Optional<TeamMember> findByMember_IdAndTeam_Id(Long memberId, Long teamId);
}
