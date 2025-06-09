package com.multi.matchon.matchup.repository;


import com.multi.matchon.common.domain.SportsTypeName;
import com.multi.matchon.matchup.domain.MatchupBoard;
import com.multi.matchon.matchup.domain.MatchupRequest;
import com.multi.matchon.matchup.dto.req.ReqMatchupRequestDto;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardListDto;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardOverviewDto;
import com.multi.matchon.member.domain.Member;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchupBoardRepository extends JpaRepository <MatchupBoard, Long> {
    //List<MatchupBoard> findAll();

    @Query("""
            select t1 
            from MatchupBoard t1 
            join fetch t1.writer 
            where t1.isDeleted=false and t1.writer.isDeleted=false
            """
            )
    List<MatchupBoard> findAllWithMember();

    @Query("""
            select t1
            from MatchupBoard t1
            join fetch t1.writer
            join fetch t1.sportsType
            where t1.isDeleted=false and t1.writer.isDeleted=false
            """
            )
    List<MatchupBoard> findAllWithMemberAndWithSportsType();

    @Query("""
            select t1
            from MatchupBoard t1
            join fetch t1.sportsType
            where t1.id=:boardId and t1.isDeleted=false and t1.writer.isDeleted=false and t1.writer =:loginMember and t1.matchDatetime>CURRENT_TIMESTAMP
            """
            )
    Optional<MatchupBoard> findMatchupBoardByBoardIdAndIsDeleted(@Param("boardId") Long boardId, @Param("loginMember") Member loginMember);


    @Query("""
            select new com.multi.matchon.matchup.dto.res
            .ResMatchupBoardListDto(
            t1.id,
            t2.id,
            t2.memberEmail,
            t2.memberName,
            t3.teamName,
            t4.sportsTypeName,
            t1.sportsFacilityName,
            t1.sportsFacilityAddress,
            t1.matchDatetime,
            t1.matchDuration,
            t1.currentParticipantCount,
            t1.maxParticipants,
            t1.minMannerTemperature,
            t1.chatRoom.id,
            t1.isRatingInitialized
            )
            from MatchupBoard t1
            join t1.writer t2
            join t2.team t3
            join t1.sportsType t4
            where (:sportsType is null or t4.sportsTypeName =:sportsType) and
                    (:region is null or t1.sportsFacilityAddress like concat('%', :region, '%')) and
                    (:matchDate is null or DATE(t1.matchDatetime) >=:matchDate) and
                    (:availableFilter =false or (:availableFilter=true and t1.currentParticipantCount<t1.maxParticipants and t1.minMannerTemperature<=:myTemperature and t1.matchDatetime>CURRENT_TIMESTAMP)) and
                    (t1.isDeleted=false and t2.isDeleted=false)
            order by t1.createdDate DESC
            """)
    Page<ResMatchupBoardListDto> findAllMatchupBoardsWithPaging(Pageable pageable, @Param("sportsType") SportsTypeName sportsType, @Param("region") String region, @Param("matchDate") LocalDate matchDate, @Param("availableFilter") Boolean availableFilter, @Param("myTemperature") Double myTemperature);

    @Query("""
            select new com.multi.matchon.matchup.dto.res
            .ResMatchupBoardListDto(
            t1.id,
            t2.id,
            t2.memberEmail,
            t2.memberName,
            t3.teamName,
            t4.sportsTypeName,
            t1.sportsFacilityName,
            t1.sportsFacilityAddress,
            t1.matchDatetime,
            t1.matchDuration,
            t1.currentParticipantCount,
            t1.maxParticipants,
            t1.minMannerTemperature,
            t1.chatRoom.id,
            t1.isRatingInitialized
            )
            from MatchupBoard t1
            join t1.writer t2
            join t2.team t3
            join t1.sportsType t4
            where (:sportsType is null or t4.sportsTypeName =:sportsType) and
                    (:matchDate is null or DATE(t1.matchDatetime) >=:matchDate) and
                    (:availableFilter =false or (:availableFilter=true and t1.matchDatetime>CURRENT_TIMESTAMP)) and
                    t1.isDeleted=false and t1.writer =:loginMember and t1.isDeleted=false and t2.isDeleted=false
            order by t1.createdDate DESC
            """)
    Page<ResMatchupBoardListDto> findAllResMatchupBoardListDtosByMemberWithPaging(Pageable pageable, @Param("loginMember") Member loginMember, @Param("sportsType") SportsTypeName sportsType, @Param("matchDate") LocalDate matchDate, @Param("availableFilter") Boolean availableFilter);


    @Query("""
            select t1
            from MatchupBoard t1
            join fetch t1.writer t2
            join fetch t1.writer.team t3
            join fetch t1.sportsType t4
            where t1.id=:boardId and t1.isDeleted=false and t2.isDeleted=false
            """)
    Optional<MatchupBoard> findMatchupBoardByBoardId(@Param("boardId") Long boardId);

    @Query("""
            select new com.multi.matchon.matchup.dto.req.ReqMatchupRequestDto(
            t1.id,
            t1.writer.memberName,
            t2.sportsTypeName,
            t1.sportsFacilityName,
            t1.sportsFacilityAddress,
            t1.matchDatetime,
            t1.matchDuration,
            t1.currentParticipantCount,
            t1.maxParticipants)
            from MatchupBoard t1 join t1.sportsType t2
            where t1.id =:boardId and t1.isDeleted=false and t1.writer.isDeleted=false
            """)
    Optional<ReqMatchupRequestDto> findReqMatchupRequestDtoByBoardId(@Param("boardId") Long boardId);

    @Query("""
            select t1
            from MatchupBoard t1
            join fetch t1.writer
            where t1.id=:boardId and t1.isDeleted=false and t1.writer.isDeleted=false
            """)
    Optional<MatchupBoard> findByIdAndIsDeletedFalse(Long boardId);


    @Query("""
            
            select
            new com.multi.matchon.matchup.dto.res.ResMatchupBoardOverviewDto(
                t1.id,
                t1.writer.memberName,
                t2.sportsTypeName,
                t1.sportsFacilityName,
                t1.sportsFacilityAddress,
                t1.matchDatetime,
                t1.matchDuration,
                t1.currentParticipantCount,
                t1.maxParticipants
            )
            from MatchupBoard t1
            join t1.sportsType t2
            where t1.id=:boardId and t1.isDeleted=false and t1.writer.isDeleted=false
            """)
    Optional<ResMatchupBoardOverviewDto> findResMatchupOverviewDto(@Param("boardId") Long boardId);


    @Query("""
            select count(t1)
            from MatchupBoard t1
            where t1.writer.id =:memberId and
                    t1.createdDate >:before24 and t1.writer.isDeleted=false
            """)
    Long countTodayMatchupBoards(@Param("memberId") Long memberId, @Param("before24") LocalDateTime before24);



//    @Query("""
//        select case
//            when t1.isDeleted=true then false
//            when t1.matchupStatus =com.multi.matchon.common.domain.Status.PENDING then true
//            when t1.matchupStatus =com.multi.matchon.common.domain.Status.APPROVED then true
//            when t1.matchupStatus =com.multi.matchon.common.domain.Status.DENIED then false
//            else false
//            end
//        from MatchupRequest t1
//        where t1.matchupBoard.id =:boardId and t1.member.id=:memberId
//
//        """)




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

//    @Query("""
//            select t2
//            from MatchupBoard t1
//            join fetch t1.matchupRequests t2
//            where t1.isDeleted=false and t1.id=:boardId and t1.matchDatetime < CURRENT_TIMESTAMP and t1.writer=:loginMember and
//            (
//                (t2.matchupStatus=com.multi.matchon.common.domain.Status.APPROVED and t2.matchupRequestSubmittedCount=1 and t2.matchupCancelSubmittedCount=0 and t2.isDeleted=false) or
//                (t2.matchupStatus=com.multi.matchon.common.domain.Status.APPROVED and t2.matchupRequestSubmittedCount=2 and t2.matchupCancelSubmittedCount=0 and t2.isDeleted=false) or
//                (t2.matchupStatus=com.multi.matchon.common.domain.Status.APPROVED and t2.matchupRequestSubmittedCount=1 and t2.matchupCancelSubmittedCount=1 and t2.isDeleted=false) or
//                (t2.matchupStatus=com.multi.matchon.common.domain.Status.APPROVED and t2.matchupRequestSubmittedCount=2 and t2.matchupCancelSubmittedCount=1 and t2.isDeleted=false) or
//                (t2.matchupStatus=com.multi.matchon.common.domain.Status.CANCELREQUESTED and t2.matchupRequestSubmittedCount=1 and t2.matchupCancelSubmittedCount=1 and t2.isDeleted=false) or
//                (t2.matchupStatus=com.multi.matchon.common.domain.Status.CANCELREQUESTED and t2.matchupRequestSubmittedCount=2 and t2.matchupCancelSubmittedCount=1 and t2.isDeleted=false)
//            )
//
//            """)
//    List<MatchupRequest> findByBoardIdAndMemberAndGameParticipantCondition(@Param("boardId") Long boardId, @Param("loginMember") Member loginMember);


    @Query("""
            select
               t1
            from MatchupBoard t1
            where t1.isDeleted =false and t1.writer=:loginMember and t1.id=:boardId and t1.matchDatetime<CURRENT_TIMESTAMP and t1.isRatingInitialized=false
            """)
    Optional<MatchupBoard> findByBoardIDAndMemberAndMatchDatetimeIsRatingInitializedFalse(@Param("boardId") Long boardId,@Param("loginMember") Member loginMember);

    @Query("""
            select
               t1
            from MatchupBoard t1
            join fetch t1.writer
            where t1.isDeleted =false and t1.matchDatetime<CURRENT_TIMESTAMP and t1.isRatingInitialized=false and t1.writer.isDeleted=false
            """)
    List<MatchupBoard> findByMatchDatetimeAndIsRatingInitializedFalse();


    @Query("""
            select t1
            from MatchupBoard t1
            join fetch t1.writer
            where t1.isDeleted =false and t1.isNotified=false and t1.matchDatetime<=:threeHoursLater and t1.writer.isDeleted=false
            """)
    List<MatchupBoard> findUnnotifiedBoardsAtThreeHoursBeforeMatch(@Param("threeHoursLater") LocalDateTime threeHoursLater);

    @Query("""
            select count(t1)
            from MatchupBoard t1
            where t1.isDeleted =false and t1.matchDatetime between :startTime and :endTime and t1.writer=:loginMember
            """)
    Long findByMemberAndStartTimeAndEndTime(@Param("loginMember") Member loginMember,@Param("startTime") LocalDateTime startTime,@Param("endTime") LocalDateTime endTime);
}
