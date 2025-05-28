package com.multi.matchon.community.service;

import com.multi.matchon.community.domain.Board;
import com.multi.matchon.community.domain.Category;
import com.multi.matchon.community.domain.Report;
import com.multi.matchon.community.domain.ReportType;
import com.multi.matchon.community.repository.BoardRepository;
import com.multi.matchon.member.domain.Member;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final CommentService commentService;

    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    public Page<Board> findAll(Pageable pageable) {
        return boardRepository.findAll(pageable);
    }

    public Page<Board> findByCategory(Category category, Pageable pageable) {
        return boardRepository.findByCategory(category, pageable);
    }

    public Board findById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    public void save(Board board) {
        boardRepository.save(board);
    }

    public Board findByAttachmentFilename(String filename) {
        return boardRepository.findAll().stream()
                .filter(board -> board.getAttachmentPath() != null &&
                        List.of(board.getAttachmentPath().split(";")).contains(filename))
                .findFirst()
                .orElse(null);
    }

    public String findSavedFilenameByPartialName(String partialFilename) {
        return boardRepository.findAll().stream()
                .flatMap(board -> {
                    if (board.getAttachmentPath() == null) return null;
                    return List.of(board.getAttachmentPath().split(";")).stream();
                })
                .filter(saved -> saved != null && saved.startsWith(partialFilename))
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public void deleteByIdAndUser(Long id, Member member) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!board.getMember().getId().equals(member.getId())) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }

        commentService.deleteAllByBoard(board);
        boardRepository.deleteById(id);
    }


}
