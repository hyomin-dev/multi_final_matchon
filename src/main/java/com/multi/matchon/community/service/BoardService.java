package com.multi.matchon.community.service;

import com.multi.matchon.community.domain.Board;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardService {

    public List<Board> getBoardsByCategory(String category, int page) {

        return List.of();
    }

    public int getTotalPagesByCategory(String category) {
        return 1;
    }
}