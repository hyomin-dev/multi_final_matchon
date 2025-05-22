package com.multi.matchon.matchup.repository;


import com.multi.matchon.common.domain.SportsTypeName;
import com.multi.matchon.matchup.domain.MatchupBoard;
import com.multi.matchon.matchup.dto.req.ReqMatchupRequestDto;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchupBoardRepository extends JpaRepository <MatchupBoard, Long> {
    List<MatchupBoard> findAll();

    @Query("select t1 from MatchupBoard t1 join fetch t1.member where t1.isDeleted=false")
    List<MatchupBoard> findAllWithMember();

    @Query("select t1 from MatchupBoard t1 join fetch t1.member join fetch t1.sportsType where t1.isDeleted=false")
    List<MatchupBoard> findAllWithMemberAndWithSportsType();

    @Query("select t1 from MatchupBoard t1 join fetch t1.sportsType where t1.id=:boardId and t1.isDeleted=false ")
    Optional<MatchupBoard> findByIdAndIsDeleted(@Param("boardId") Long boardId);


    @Query("""
            select new com.multi.matchon.matchup.dto.res
            .ResMatchupBoardListDto( 
            t1.id,
            t2.memberEmail,
            t3.teamName,
            t4.sportsTypeName,
            t1.sportsFacilityName,
            t1.sportsFacilityAddress,
            t1.matchDatetime,
            t1.matchDuration,
            t1.currentParticipantCount,
            t1.maxParticipants,
            t1.minMannerTemperature)
            from MatchupBoard t1
            join t1.member t2
            join t2.team t3
            join t1.sportsType t4
            where (:sportsType is null or t4.sportsTypeName =:sportsType) and
                    (:region is null or t1.sportsFacilityAddress like concat('%', :region, '%')) and
                    (:matchDate is null or DATE(t1.matchDatetime) >=:matchDate) and
                    (t1.isDeleted=false)
            order by t1.createdDate DESC
            """)
    Page<ResMatchupBoardListDto> findBoardListWithPaging(Pageable pageable, @Param("sportsType") SportsTypeName sportsType, @Param("region") String region, @Param("matchDate") LocalDate matchDate);

    @Query("""
            select new com.multi.matchon.matchup.dto.res
            .ResMatchupBoardListDto( 
            t1.id,
            t2.memberEmail,
            t3.teamName,
            t4.sportsTypeName,
            t1.sportsFacilityName,
            t1.sportsFacilityAddress,
            t1.matchDatetime,
            t1.matchDuration,
            t1.currentParticipantCount,
            t1.maxParticipants,
            t1.minMannerTemperature)
            from MatchupBoard t1
            join t1.member t2
            join t2.team t3
            join t1.sportsType t4
            where t2.memberEmail =:email and t1.isDeleted=false
            order by t1.matchDatetime DESC
            """)
    Page<ResMatchupBoardListDto> findByMemberEmailBoardListWithPaging(Pageable pageable, @Param("email") String email);


    @Query("select t1 from MatchupBoard t1 join fetch t1.member t2 join fetch t1.member.team t3 join fetch t1.sportsType t4 where t1.id=:boardId and t1.isDeleted=false")
    Optional<MatchupBoard> findByIdWithMemberWithTeamWithSportsType(@Param("boardId") Long boardId);


    @Query("""
            select new com.multi.matchon.matchup.dto.req.ReqMatchupRequestDto(
            t1.id,
            t2.sportsTypeName,
            t1.sportsFacilityName,
            t1.sportsFacilityAddress,
            t1.matchDatetime,
            t1.matchDuration,
            t1.currentParticipantCount,
            t1.maxParticipants)
            from MatchupBoard t1 join t1.sportsType t2
            where t1.id =:boardId and t1.isDeleted=false
            """)
    Optional<ReqMatchupRequestDto> findReqMatchupRequestDtoByBoardId(@Param("boardId") Long boardId);


//    @Query("""
//            select new com.multi.matchon.matchup.dto.res
//            .ResMatchupBoardListDto(
//            t1.id,
//            t2.memberEmail,
//            t3.teamName,
//            t4.sportsTypeName,
//            t1.sportsFacilityName,
//            t1.sportsFacilityAddress,
//            t1.matchDatetime,
//            t1.matchDuration,
//            t1.currentParticipantCount,
//            t1.maxParticipants,
//            t1.minMannerTemperature)
//            from MatchupBoard t1
//            join t1.member t2
//            join t2.team t3
//            join t1.sportsType t4
//            order by t1.id DESC
//            """)
//    List<ResMatchupBoardListDto> findBoardListTest();
//
//    @Query("""
//            select new com.multi.matchon.matchup.dto.res
//            .ResMatchupBoardListDto(
//            t1.id,
//            t2.memberEmail,
//            t3.teamName,
//            t4.sportsTypeName,
//            t1.sportsFacilityName,
//            t1.sportsFacilityAddress,
//            t1.matchDatetime,
//            t1.matchDuration,
//            t1.currentParticipantCount,
//            t1.maxParticipants,
//            t1.minMannerTemperature)
//            from MatchupBoard t1
//            join t1.member t2
//            join t2.team t3
//            join t1.sportsType t4
//            order by t1.createdDate DESC
//            """)
//    Page<ResMatchupBoardListDto> findBoardListTest2(Pageable pageable);



}
