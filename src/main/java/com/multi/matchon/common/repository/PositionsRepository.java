package com.multi.matchon.common.repository;

import com.multi.matchon.common.domain.PositionName;
import com.multi.matchon.common.domain.Positions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionsRepository extends JpaRepository<Positions, Long> {


    Positions findByPositionName  (PositionName sportsTypeId);

}
