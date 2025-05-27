package com.multi.matchon.team.repository;

import com.multi.matchon.team.domain.TeamJoinRequest;
import com.multi.matchon.common.domain.Status;
import com.multi.matchon.team.domain.Team;
import com.multi.matchon.member.domain.Member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamJoinRequestRepository extends JpaRepository<TeamJoinRequest, Long> {

    // 1. Find all PENDING requests for a given team
    List<TeamJoinRequest> findByTeamAndJoinRequestStatusAndIsDeletedFalse(Team team, Status joinRequestStatus);

    // 2. (Optional) Find existing request by member and team (e.g., to prevent duplicates)
    Optional<TeamJoinRequest> findByTeamAndMemberAndIsDeletedFalse(Team team, Member member);

    // 3. (Optional) Find all requests by member (e.g., show user's application history)
    List<TeamJoinRequest> findByMemberAndIsDeletedFalse(Member member);

    List<TeamJoinRequest> findByTeamIdAndJoinRequestStatus(Long teamId, Status status);

    boolean existsByMemberAndTeamAndIsDeletedFalse(Member member, Team team);
}