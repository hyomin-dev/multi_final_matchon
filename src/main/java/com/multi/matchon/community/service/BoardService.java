package com.multi.matchon.community.service;

import com.multi.matchon.community.domain.Board;
import com.multi.matchon.community.domain.Category;
import com.multi.matchon.community.repository.BoardRepository;
import com.multi.matchon.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final CommentService commentService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    public Board findById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Board not found"));
    }

    public void save(Board board) {
        boardRepository.save(board);
    }

    public Page<Board> findAll(Pageable pageable) {
        return boardRepository.findAll(pageable);
    }

    public Page<Board> findByCategory(Category category, Pageable pageable) {
        return boardRepository.findByCategory(category, pageable);
    }

    public Board findByAttachmentFilename(String filename) {
        List<Board> boards = boardRepository.findAll(); // 성능 최적화 필요 시 쿼리로 개선
        for (Board board : boards) {
            if (board.getAttachmentPath() != null &&
                    List.of(board.getAttachmentPath().split(";")).contains(filename)) {
                return board;
            }
        }
        return null;
    }

    @Transactional
    public void deleteByIdAndUser(Long id, Member member) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!board.getMember().getId().equals(member.getId())) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }

        // 1. 댓글 먼저 삭제
        commentService.deleteAllByBoard(board);

        // 2. 게시글 삭제
        boardRepository.deleteById(id);
      
    }

}

