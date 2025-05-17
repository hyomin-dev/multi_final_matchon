package com.multi.matchon.matchup.service;


import com.multi.matchon.matchup.domain.MatchupBoard;

import java.util.List;

public interface MatchupService {

    List<MatchupBoard> findAll();
}
