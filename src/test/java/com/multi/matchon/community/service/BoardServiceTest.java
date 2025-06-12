package com.multi.matchon.community.service;

import com.multi.matchon.common.domain.Attachment;
import com.multi.matchon.common.domain.BoardType;
import com.multi.matchon.common.repository.AttachmentRepository;
import com.multi.matchon.community.domain.Board;
import com.multi.matchon.community.domain.Category;
import com.multi.matchon.community.dto.res.BoardListResponse;
import com.multi.matchon.community.repository.BoardRepository;
import com.multi.matchon.community.repository.CommentRepository;
import com.multi.matchon.member.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class BoardServiceTest {

    @InjectMocks
    private BoardService boardService;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private CommentService commentService;

    private Member 작성자;

    @BeforeEach
    void setUp() {
        작성자 = Member.builder().id(1L).memberName("작성자").build();
    }

    @Test
    void 게시글_전체_조회() {
        List<Board> mockList = List.of(Board.builder().id(1L).title("글제목").member(작성자).build());
        when(boardRepository.findAll()).thenReturn(mockList);

        List<Board> result = boardService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("글제목");
    }

    @Test
    void 카테고리별_게시글_조회_성공() {
        Category category = Category.FREEBOARD;
        Pageable pageable = PageRequest.of(0, 5);
        Page<Board> page = new PageImpl<>(List.of(Board.builder().id(1L).category(category).member(작성자).build()));
        when(boardRepository.findByCategory(category, pageable)).thenReturn(page);

        Page<Board> result = boardService.findByCategory(category, pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void 게시글_아이디로_조회_성공() {
        Board board = Board.builder().id(10L).title("조회 테스트").member(작성자).build();
        when(boardRepository.findById(10L)).thenReturn(Optional.of(board));

        Board result = boardService.findById(10L);

        assertThat(result.getTitle()).isEqualTo("조회 테스트");
    }

    @Test
    void 존재하지_않는_게시글_조회시_예외발생() {
        when(boardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> boardService.findById(999L));
    }

    @Test
    void 게시글_삭제시_댓글과_첨부파일도_삭제된다() {
        Board board = Board.builder().id(20L).member(작성자).build();
        when(boardRepository.findById(20L)).thenReturn(Optional.of(board));
        when(attachmentRepository.findAllByBoardTypeAndBoardNumber(BoardType.BOARD, 20L))
                .thenReturn(List.of(new Attachment()));

        boardService.deleteById(20L);

        verify(commentService).deleteAllByBoard(board);
        verify(attachmentRepository).findAllByBoardTypeAndBoardNumber(BoardType.BOARD, 20L);
        verify(boardRepository).deleteById(20L);
    }

    @Test
    void 게시글_댓글수포함_목록_조회_성공() {
        Board board = Board.builder()
                .id(1L)
                .title("글")
                .member(작성자)
                .category(Category.FREEBOARD)
                .pinned(true)
                .build();

        Page<Board> boardPage = new PageImpl<>(List.of(board));
        when(boardRepository.findByCategory(Category.FREEBOARD, PageRequest.of(0, 10))).thenReturn(boardPage);
        when(commentRepository.countByBoardIdAndIsDeletedFalse(1L)).thenReturn(3);

        Page<BoardListResponse> result = boardService.findBoardsWithCommentCount(
                Category.FREEBOARD, PageRequest.of(0, 10));

        assertThat(result.getContent().get(0).getCommentCount()).isEqualTo(3);
    }
}
