package com.multi.matchon.community.service;

import com.multi.matchon.community.domain.Board;
import com.multi.matchon.community.domain.Comment;
import com.multi.matchon.community.repository.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    private Board 게시글;
    private Comment 댓글;

    @BeforeEach
    void setUp() {
        게시글 = Board.builder().id(1L).title("테스트 게시글").build();
        댓글 = Comment.builder().id(10L).content("댓글 내용").board(게시글).isDeleted(false).build();
    }

    @Test
    void 게시글로부터_삭제되지_않은_댓글_목록_조회() {
        when(commentRepository.findByBoardAndIsDeletedFalse(게시글)).thenReturn(List.of(댓글));

        List<Comment> result = commentService.getCommentsByBoard(게시글);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("댓글 내용");
    }

    @Test
    void 댓글_저장_성공() {
        when(commentRepository.save(댓글)).thenReturn(댓글);

        Comment saved = commentService.save(댓글);

        assertThat(saved.getId()).isEqualTo(10L);
        verify(commentRepository).save(댓글);
    }

    @Test
    void 아이디로_댓글_조회_성공() {
        when(commentRepository.findById(10L)).thenReturn(Optional.of(댓글));

        Comment found = commentService.findById(10L);

        assertThat(found.getContent()).isEqualTo("댓글 내용");
    }

    @Test
    void 존재하지_않는_댓글_조회시_예외발생() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("댓글을 찾을 수 없습니다.");
    }

    @Test
    void 댓글_소프트삭제_처리_성공() {
        when(commentRepository.findById(10L)).thenReturn(Optional.of(댓글));

        commentService.softDelete(10L);

        assertThat(댓글.getIsDeleted()).isTrue();
        verify(commentRepository).save(댓글);
    }

    @Test
    void 게시글에_연결된_모든_댓글_삭제() {
        commentService.deleteAllByBoard(게시글);

        verify(commentRepository).deleteAllByBoard(게시글);
    }
}
