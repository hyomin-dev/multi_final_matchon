package com.multi.matchon.team.repository;

import com.multi.matchon.common.domain.PositionName;
import com.multi.matchon.team.domain.RegionType;
import com.multi.matchon.team.domain.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;


import java.util.List;

public interface TeamNameRepository extends JpaRepository <Team, Long> {
    List<Team> findAll();

    @Query("""

            SELECT DISTINCT t FROM Team t
LEFT JOIN t.recruitingPositions rp
LEFT JOIN rp.positions p
WHERE (:positionName IS NULL OR p.positionName = :positionName)
AND (:region IS NULL OR t.teamRegion = :region)
AND (t.teamRatingAverage IS NULL OR t.teamRatingAverage >= :rating)
""")
    Page<Team> findTeamListWithPaging(
            @Param("positionName") PositionName positionName,
            @Param("region") RegionType region,
            @Param("rating") Double rating,
            Pageable pageable);
    }
