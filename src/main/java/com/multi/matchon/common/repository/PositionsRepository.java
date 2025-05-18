package com.multi.matchon.common.repository;

import com.multi.matchon.common.domain.Positions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PositionsRepository extends JpaRepository<Positions, Long> {

    //List<Positions> findBySportsTypeId(Long sportsTypeId);

}
