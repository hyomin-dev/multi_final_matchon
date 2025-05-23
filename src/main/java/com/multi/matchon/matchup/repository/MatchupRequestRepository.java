package com.multi.matchon.matchup.repository;


import com.multi.matchon.common.domain.SportsTypeName;
import com.multi.matchon.common.dto.res.PageResponseDto;
import com.multi.matchon.matchup.domain.MatchupRequest;
import com.multi.matchon.matchup.dto.req.ReqMatchupRequestDto;
import com.multi.matchon.matchup.dto.res.ResMatchupRequestDto;
import com.multi.matchon.matchup.dto.res.ResMatchupRequestListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MatchupRequestRepository extends JpaRepository<MatchupRequest, Long> {

    @Query("""
            
            select new com.multi.matchon.matchup.dto.res.ResMatchupRequestListDto(
            t2.id,
            t1.id,
            t3.sportsTypeName,
            t2.sportsFacilityName,
            t2.sportsFacilityAddress,
            t2.matchDatetime,
            t2.matchDuration,
            t2.currentParticipantCount,
            t2.maxParticipants,
            t1.participantCount,
            t1.matchupStatus
            )
            from MatchupRequest t1
            join t1.matchupBoard t2
            join t2.sportsType t3
            where t1.member.id=:memberId and
                    (:sportsType is null or t3.sportsTypeName =:sportsType) and
                    (:matchDate is null or DATE(t2.matchDatetime) >=:matchDate) and
                    (t1.isDeleted=false)
            order by t2.matchDatetime DESC
            """)
    Page<ResMatchupRequestListDto> findAllResMatchupRequestListDtosByMemberIdAndSportsTypeAndMatchDateWithPaging(PageRequest pageRequest, @Param("memberId") Long memberId, @Param("sportsType") SportsTypeName sportsTypeName, @Param("matchDate") LocalDate matchDate);


    @Query("""
            select new com.multi.matchon.matchup.dto.res.ResMatchupRequestDto(
            t5.memberEmail,
            t5.memberName,
            t2.memberEmail,
            t2.memberName,
            t3.id,
            t1.id,
            t4.sportsTypeName,
            t3.sportsFacilityName,
            t3.sportsFacilityAddress,
            t3.matchDatetime,
            t3.matchDuration,
            t3.currentParticipantCount,
            t3.maxParticipants,
            t1.participantCount,
            t1.matchupStatus,
            t1.selfIntro
            )
            from MatchupRequest t1
            join t1.member t2
            join t1.matchupBoard t3
            join t3.sportsType t4
            join t3.member t5
            where t1.id=:requestId and t1.isDeleted=false
            """)
    Optional<ResMatchupRequestDto> findResMatchupRequestDtoByRequestId(Long requestId);

    @Query("""
            select case
                    when count(t1)>0 then true
                    else false
                end
                from MatchupRequest t1
                where t1.matchupBoard.id =:boardId and t1.member.id =:memberId and t1.isDeleted=true
            """)
    Boolean isCanceledMatchupRequestByBoardIdAndMemberId(@Param("boardId") Long boardId, @Param("memberId") Long memberId);

    @Query("""
        select case
                when count(t1) > 0 then true
                else false
           end
        from MatchupRequest t1
        where (t1.matchupBoard.id =:boardId and t1.member.id=:memberId and t1.isDeleted=false) and
                  t1.matchupStatus in (
                    com.multi.matchon.common.domain.Status.PENDING,
                    com.multi.matchon.common.domain.Status.APPROVED,
                    com.multi.matchon.common.domain.Status.CANCELREQUESTED
                )
        """)
    Boolean isAlreadyMatchupRequestedByBoardIdAndMemberId(@Param("boardId") Long boardId,@Param("memberId") Long memberId); // true: 중복된 요청이 존재, false: 중복된 요청이 없음

    @Query("""
            select case
                    when count(t1) >0 then true
                    else false
                end
            from MatchupRequest t1
            where t1.matchupBoard.id =:boardId and t1.member.id =:memberId and t1.isDeleted=false and
                t1.matchupStatus =com.multi.matchon.common.domain.Status.DENIED and
                t1.matchupRequestResubmittedCount>=2
            """)
    Boolean hasExceededThreeMatchupRequestsByBoardIdAndMemberId(@Param("boardId") Long boardId, @Param("memberId") Long memberId);



}
