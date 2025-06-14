package com.multi.matchon.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multi.matchon.community.domain.Board;
import com.multi.matchon.community.domain.Comment;
import com.multi.matchon.community.dto.req.CommentRequest;
import com.multi.matchon.community.service.BoardService;
import com.multi.matchon.community.service.CommentService;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.domain.MemberRole;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @MockBean
    private BoardService boardService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void 댓글_작성_성공() throws Exception {
        Member member = Member.builder().id(1L).memberName("홍길동").memberRole(MemberRole.USER).build();
        Board board = Board.builder().id(100L).member(member).build();
        Comment comment = Comment.builder()
                .id(10L)
                .member(member)
                .board(board)
                .content("댓글 내용입니다")
                .build();

        CommentRequest request = new CommentRequest();
        ReflectionTestUtils.setField(request, "content", "댓글 내용입니다");

        given(boardService.findById(100L)).willReturn(board);
        given(commentService.save(ArgumentMatchers.any(Comment.class))).willReturn(comment);

        mockMvc.perform(post("/community/100/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("댓글 내용입니다"))
                .andExpect(jsonPath("$.writer").value("홍길동"))
                .andExpect(jsonPath("$.commentId").value(10L));
    }

    @Test
    void 로그인하지_않은_사용자는_댓글_작성시_401_반환() throws Exception {
        CommentRequest request = new CommentRequest();
        ReflectionTestUtils.setField(request, "content", "비로그인 사용자 댓글");

        mockMvc.perform(post("/community/100/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인이 필요합니다."));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void 관리자는_댓글_삭제_가능() throws Exception {
        Member member = Member.builder().id(1L).memberRole(MemberRole.ADMIN).build();
        Comment comment = Comment.builder().id(10L).member(member).build();

        given(commentService.findById(10L)).willReturn(comment);
        doNothing().when(commentService).softDelete(10L);

        mockMvc.perform(delete("/community/100/comments/10"))
                .andExpect(status().isOk())
                .andExpect(content().string("삭제 성공"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void 본인_댓글이_아니면_삭제시_403_반환() throws Exception {
        Member owner = Member.builder().id(1L).memberRole(MemberRole.USER).build();
        Member anotherUser = Member.builder().id(2L).memberRole(MemberRole.USER).build();

        Comment comment = Comment.builder().id(10L).member(owner).build();

        given(commentService.findById(10L)).willReturn(comment);

        mockMvc.perform(delete("/community/100/comments/10"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("삭제 권한이 없습니다."));
    }

    @Test
    void 로그인하지_않은_사용자는_댓글_삭제시_401_반환() throws Exception {
        mockMvc.perform(delete("/community/100/comments/10"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인이 필요합니다."));
    }
}
