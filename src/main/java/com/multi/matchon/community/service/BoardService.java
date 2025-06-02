package com.multi.matchon.community.service;

import com.multi.matchon.community.domain.Board;
import com.multi.matchon.community.domain.Category;
import com.multi.matchon.community.repository.BoardRepository;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.common.domain.Attachment;
import com.multi.matchon.common.domain.BoardType;
import com.multi.matchon.common.repository.AttachmentRepository;

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
    private final AttachmentRepository attachmentRepository; // 첨부파일 repository 주입

    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    public Page<Board> findAll(Pageable pageable) {
        return boardRepository.findAll(pageable);
    }

    public Page<Board> findByCategory(Category category, Pageable pageable) {
        return boardRepository.findByCategory(category, pageable);
    }

    public List<Board> findPinnedByCategory(Category category) {
        return boardRepository.findByCategoryAndPinnedTrueOrderByCreatedDateDesc(category);
    }

    public Board findById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    public void save(Board board) {
        boardRepository.save(board);
    }

    @Transactional
    public void deleteByIdAndUser(Long id, Member member) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!board.getMember().getId().equals(member.getId())) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }

        // 댓글 먼저 삭제
        commentService.deleteAllByBoard(board);

        // 첨부파일 소프트 삭제
        softDeleteCommunityAttachments(id);

        // 게시글 삭제
        boardRepository.deleteById(id);
    }

    private void softDeleteCommunityAttachments(Long boardId) {
        List<Attachment> attachments = attachmentRepository.findAllByBoardTypeAndBoardNumber(BoardType.BOARD, boardId);
        for (Attachment att : attachments) {
            att.delete(true);
        }
    }

}
