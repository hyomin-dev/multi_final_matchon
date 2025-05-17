package com.multi.matchon.matchup.repository;


import com.multi.matchon.matchup.domain.MatchupBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchupBoardRepository extends JpaRepository <MatchupBoard, Long> {
    List<MatchupBoard> findAll();

    @Query("select t1 from MatchupBoard t1 join fetch t1.member")
    List<MatchupBoard> findAllWithMember();

    @Query("select t1 from MatchupBoard t1 join fetch t1.member join fetch t1.sportsType")
    List<MatchupBoard> findAllWithMemberAndWithSportsType();
}
