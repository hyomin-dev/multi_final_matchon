package com.multi.matchon.community.service;

import com.multi.matchon.community.domain.Board;
import com.multi.matchon.community.repository.BoardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;

    @Override
    public Board createBoard(Board board) {
        return boardRepository.save(board);
    }

    @Override
    public Board getBoardById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Board not found with id: " + id));
    }

    @Override
    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    @Override
    public Board updateBoard(Long id, Board updatedBoard) {
        Board existingBoard = getBoardById(id);
        existingBoard = Board.builder()
                .id(existingBoard.getId())
                .title(updatedBoard.getTitle())
                .content(updatedBoard.getContent())
                .boardAttachmentEnabled(updatedBoard.getBoardAttachmentEnabled())
                .category(updatedBoard.getCategory())
                .member(existingBoard.getMember()) // 작성자는 수정 불가
                .isDeleted(existingBoard.getIsDeleted())
                .build();
        return boardRepository.save(existingBoard);
    }

    @Override
    public void deleteBoard(Long id) {
        Board board = getBoardById(id);
        boardRepository.delete(board); // 또는 소프트 삭제 처리
    }
}

