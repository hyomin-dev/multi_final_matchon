package com.multi.matchon.community.controller;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.community.domain.Board;
import com.multi.matchon.community.domain.Comment;
import com.multi.matchon.community.dto.req.CommentRequest;
import com.multi.matchon.community.service.BoardService;
import com.multi.matchon.community.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final BoardService boardService;

    @PostMapping("/{id}/comments")
    @ResponseBody
    public ResponseEntity<?> addCommentAjax(@PathVariable Long id,
                                            @Valid @RequestBody CommentRequest commentRequest,
                                            BindingResult bindingResult,
                                            @AuthenticationPrincipal CustomUser userDetails) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("댓글 내용은 비워둘 수 없습니다.");
        }

        Board board = boardService.findById(id);

        Comment comment = Comment.builder()
                .board(board)
                .member(userDetails.getMember())
                .content(commentRequest.getContent())
                .build();

        Comment savedComment = commentService.save(comment);

        return ResponseEntity.ok(Map.of(
                "memberName", savedComment.getMember().getMemberName(),
                "createdDate", savedComment.getCreatedDate().toString(),
                "content", savedComment.getContent(),
                "commentId", savedComment.getId()
        ));
    }

    @DeleteMapping("/{boardId}/comments/{commentId}")
    @ResponseBody
    public ResponseEntity<?> deleteCommentAjax(@PathVariable Long boardId,
                                               @PathVariable Long commentId,
                                               @AuthenticationPrincipal CustomUser userDetails) {
        Comment comment = commentService.findById(commentId);

        if (!comment.getMember().getId().equals(userDetails.getMember().getId())) {
            return ResponseEntity.status(403).body("삭제 권한이 없습니다.");
        }

        commentService.softDelete(commentId);
        return ResponseEntity.ok().body("삭제 성공");
    }
}


