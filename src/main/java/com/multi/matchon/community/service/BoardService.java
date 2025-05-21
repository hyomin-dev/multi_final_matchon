package com.multi.matchon.community.service;

import com.multi.matchon.community.domain.Board;
import java.util.List;

public interface BoardService {
    Board createBoard(Board board);
    Board getBoardById(Long id);
    List<Board> getAllBoards();
    Board updateBoard(Long id, Board updatedBoard);
    void deleteBoard(Long id);
}
