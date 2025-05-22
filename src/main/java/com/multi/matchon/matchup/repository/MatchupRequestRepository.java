package com.multi.matchon.matchup.repository;


import com.multi.matchon.matchup.domain.MatchupRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchupRequestRepository extends JpaRepository<MatchupRequest, Long> {
}
