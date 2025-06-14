package com.multi.matchon.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.JwtProvider;
import com.multi.matchon.common.util.AwsS3Utils;
import com.multi.matchon.community.domain.Board;
import com.multi.matchon.community.domain.Category;
import com.multi.matchon.community.dto.req.BoardRequest;
import com.multi.matchon.community.service.BoardService;
import com.multi.matchon.community.service.CommentService;
import com.multi.matchon.community.service.ReportService;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.domain.MemberRole;
import io.awspring.cloud.s3.S3Operations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import software.amazon.awssdk.services.s3.S3Client;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 실제 컨텍스트의 서비스 로드 가능하나, 테스트 격리를 위해 MockBean 유지
    @MockBean
    private BoardService boardService;

    @MockBean
    private CommentService commentService;

    @MockBean
    private ReportService reportService;

    // 외부 연동은 반드시 Mock 처리
    @MockBean
    private AwsS3Utils awsS3Utils;

    @MockBean
    private S3Operations s3Operations;

    @MockBean
    private S3Client s3Client;

    @MockBean
    private JwtProvider jwtProvider;

    @Test
    void 게시판_목록_페이지_조회_성공() throws Exception {
        mockMvc.perform(get("/community")
                        .param("category", "FREEBOARD")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("community/view"))
                .andExpect(model().attributeExists("boardsPage"))
                .andExpect(model().attributeExists("selectedCategory"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("pinnedPosts"))
                .andExpect(model().attributeExists("pageNumbers"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void 일반_사용자는_게시글_작성페이지_접근시_리다이렉트된다() throws Exception {
        mockMvc.perform(get("/community/new"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void 관리자_게시글_작성_성공() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "test.txt", "text/plain", "file content".getBytes());

        BoardRequest boardRequest = new BoardRequest("제목", "내용", Category.FREEBOARD, false);

        mockMvc.perform(multipart("/community")
                        .file(file)
                        .param("title", boardRequest.getTitle())
                        .param("content", boardRequest.getContent())
                        .param("category", boardRequest.getCategory().name())
                        .with(request -> {
                            request.setMethod("POST");
                            return request;
                        }))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void 관리자_게시글_삭제_성공() throws Exception {
        Member member = Member.builder()
                .id(1L)
                .memberName("관리자")
                .memberRole(MemberRole.ADMIN)
                .build();

        given(boardService.findById(1L)).willReturn(
                Board.builder().id(1L).member(member).build()
        );

        doNothing().when(boardService).deleteById(1L);

        mockMvc.perform(post("/community/1/delete"))
                .andExpect(status().isOk());
    }
}
