package com.multi.matchon.matchup.repository;


import com.multi.matchon.matchup.domain.MatchupBoard;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


    @Query("""
            select new com.multi.matchon.matchup.dto.res
            .ResMatchupBoardListDto( 
            t1.id,
            t2.memberEmail,
            t3.teamName,
            t4.sportsTypeName,
            t1.sportsFacilityName,
            t1.matchDatetime,
            t1.matchDuration,
            t1.currentParticipantCount,
            t1.maxParticipants,
            t1.minMannerTemperature)
            from MatchupBoard t1
            join t1.member t2
            join t2.team t3
            join t1.sportsType t4
            order by t1.createdDate DESC
            """)
    Page<ResMatchupBoardListDto> findBoardListWithPaging(Pageable pageable);


    @Query("""
            select new com.multi.matchon.matchup.dto.res
            .ResMatchupBoardListDto( 
            t1.id,
            t2.memberEmail,
            t3.teamName,
            t4.sportsTypeName,
            t1.sportsFacilityName,
            t1.matchDatetime,
            t1.matchDuration,
            t1.currentParticipantCount,
            t1.maxParticipants,
            t1.minMannerTemperature)
            from MatchupBoard t1
            join t1.member t2
            join t2.team t3
            join t1.sportsType t4
            order by t1.id DESC
            """)
    List<ResMatchupBoardListDto> findBoardListTest();

    @Query("""
            select new com.multi.matchon.matchup.dto.res
            .ResMatchupBoardListDto( 
            t1.id,
            t2.memberEmail,
            t3.teamName,
            t4.sportsTypeName,
            t1.sportsFacilityName,
            t1.matchDatetime,
            t1.matchDuration,
            t1.currentParticipantCount,
            t1.maxParticipants,
            t1.minMannerTemperature)
            from MatchupBoard t1
            join t1.member t2
            join t2.team t3
            join t1.sportsType t4
            order by t1.createdDate DESC
            """)
    Page<ResMatchupBoardListDto> findBoardListTest2(Pageable pageable);



}
