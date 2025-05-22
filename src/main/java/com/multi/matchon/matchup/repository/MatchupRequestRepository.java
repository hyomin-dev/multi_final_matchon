package com.multi.matchon.matchup.repository;


import com.multi.matchon.common.domain.SportsTypeName;
import com.multi.matchon.common.dto.res.PageResponseDto;
import com.multi.matchon.matchup.domain.MatchupRequest;
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
            t1.participantCount,
            t1.matchupStatus
            )
            from MatchupRequest t1
            join t1.matchupBoard t2
            join t2.sportsType t3
            where t1.member.id=:id and
                    (:sportsType is null or t3.sportsTypeName =:sportsType) and
                    (:matchDate is null or DATE(t2.matchDatetime) >=:matchDate) and
                    (t1.isDeleted=false)
            order by t2.matchDatetime DESC
            """)
    Page<ResMatchupRequestListDto> findMyRequestAllWithPaging(PageRequest pageRequest, @Param("id") Long id, @Param("sportsType") SportsTypeName sportsTypeName, @Param("matchDate") LocalDate matchDate);


    @Query("""
            select new com.multi.matchon.matchup.dto.res.ResMatchupRequestDto(
            t5.memberEmail,
            t2.memberEmail,
            t3.id,
            t1.id,
            t4.sportsTypeName,
            t3.sportsFacilityName,
            t3.sportsFacilityAddress,
            t3.matchDatetime,
            t3.matchDuration,
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
    Optional<ResMatchupRequestDto> findResMatchRequestDtoByRequestId(Long requestId);
}
