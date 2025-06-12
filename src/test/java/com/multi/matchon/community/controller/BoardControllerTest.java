package com.multi.matchon.community.controller;

import com.multi.matchon.community.domain.*;
import com.multi.matchon.community.service.BoardService;
import com.multi.matchon.community.service.CommentService;
import com.multi.matchon.community.service.ReportService;
import com.multi.matchon.common.domain.Attachment;
import com.multi.matchon.common.domain.BoardType;
import com.multi.matchon.common.repository.AttachmentRepository;
import com.multi.matchon.common.util.AwsS3Utils;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.domain.MemberRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("removal")
@WebMvcTest(BoardController.class)
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private BoardService boardService;
    @MockBean private CommentService commentService;
    @MockBean private ReportService reportService;
    @MockBean private AwsS3Utils awsS3Utils;
    @MockBean private AttachmentRepository attachmentRepository;

    @Test
    void 게시글_상세_조회_테스트() throws Exception {
        // given
        Member member = Member.builder()
                .id(1L)
                .memberName("홍길동")
                .memberRole(MemberRole.USER)
                .build();

        Board board = Board.builder()
                .id(1L)
                .title("테스트 제목")
                .content("테스트 내용")
                .category(Category.FREEBOARD)
                .member(member)
                .build();

        given(boardService.findById(1L)).willReturn(board);
        given(attachmentRepository.findAllByBoardTypeAndBoardNumber(BoardType.BOARD, 1L))
                .willReturn(Collections.emptyList());
        given(commentService.getCommentsByBoard(board))
                .willReturn(Collections.emptyList());

        // AwsS3Utils 내부 메서드가 호출될 경우를 대비한 Mock 처리 (필요한 경우만)
        given(awsS3Utils.getObjectUrl(anyString(), anyString(), any()))
                .willReturn("https://s3-fake-url.com/fake.png");
        doNothing().when(awsS3Utils).saveFile(anyString(), anyString(), any());
        doNothing().when(awsS3Utils).deleteFile(anyString(), anyString());

        // when + then
        mockMvc.perform(get("/community/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("community/detail"))
                .andExpect(model().attributeExists("board"))
                .andExpect(model().attributeExists("attachments"))
                .andExpect(model().attributeExists("commentRequest"))
                .andExpect(model().attributeExists("comments"));
    }
}
