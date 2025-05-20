package com.multi.matchon.team.repository;

import com.multi.matchon.team.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamNameRepository extends JpaRepository <Team, Long> {
    List<Team> findAll();
}
