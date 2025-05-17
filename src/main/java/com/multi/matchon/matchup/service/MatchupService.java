package com.multi.matchon.matchup.service;

import com.multi.matchon.matchup.domain.MatchupBoard;
import com.multi.matchon.matchup.repository.MatchupBoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MatchupService{

    private final MatchupBoardRepository matchupBoardRepository;

    public List<MatchupBoard> findAll() {
        List<MatchupBoard> matchupBoards = matchupBoardRepository.findAll();
        List<MatchupBoard> matchupBoards1 = matchupBoardRepository.findAllWithMember();
        List<MatchupBoard> matchupBoards2 = matchupBoardRepository.findAllWithMemberAndWithSportsType();

        return matchupBoards;
    }
}
