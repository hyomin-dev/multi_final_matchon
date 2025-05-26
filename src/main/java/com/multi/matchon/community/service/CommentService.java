package com.multi.matchon.community.service;

import com.multi.matchon.community.domain.Board;
import com.multi.matchon.community.domain.Comment;
import com.multi.matchon.community.repository.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public List<Comment> getCommentsByBoard(Board board) {
        return commentRepository.findByBoardAndIsDeletedFalse(board);
    }

    public Comment save(Comment comment) {
        commentRepository.save(comment);
        return comment;
    }

    public Comment findById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));
    }

    public void softDelete(Long commentId) {
        Comment comment = findById(commentId);
        comment.setIsDeleted(true);
        commentRepository.save(comment);
    }


    @Transactional
    public void deleteAllByBoard(Board board) {
        commentRepository.deleteAllByBoard(board);
    }


}

