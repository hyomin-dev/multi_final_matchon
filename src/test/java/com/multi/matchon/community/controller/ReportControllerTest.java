package com.multi.matchon.community.controller;

import com.multi.matchon.community.domain.ReasonType;
import com.multi.matchon.community.domain.ReportType;
import com.multi.matchon.community.dto.res.ReportResponse;
import com.multi.matchon.community.service.ReportService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void 로그인된_사용자는_신고를_정상적으로_등록할_수_있다() throws Exception {
        doNothing().when(reportService).report(
                ArgumentMatchers.eq(ReportType.BOARD),
                ArgumentMatchers.eq(1L),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.eq(ReasonType.SPAM),
                ArgumentMatchers.any()
        );

        mockMvc.perform(post("/community/reports")
                        .param("type", "BOARD")
                        .param("targetId", "1")
                        .param("reason", "스팸입니다")
                        .param("reasonType", "SPAM"))
                .andExpect(status().isOk())
                .andExpect(content().string("신고가 접수되었습니다."));
    }

    @Test
    void 비로그인_사용자는_신고시_401_반환() throws Exception {
        mockMvc.perform(post("/community/reports")
                        .param("type", "BOARD")
                        .param("targetId", "1")
                        .param("reason", "스팸입니다")
                        .param("reasonType", "SPAM"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인이 필요합니다."));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void 관리자는_신고_목록_페이지를_조회할_수_있다() throws Exception {
        ReportResponse report1 = ReportResponse.builder()
                .id(1L)
                .reportType("게시글")
                .targetId(101L)
                .targetWriterName("홍길동")
                .reporterName("관리자")
                .reasonType("SPAM")
                .reason("부적절")
                .createdDate(LocalDateTime.now())
                .boardId(101L)
                .targetMemberId(11L)
                .targetExists(true)
                .suspended(false)
                .targetIsAdmin(false)
                .build();

        List<ReportResponse> reportList = List.of(report1);
        Page<ReportResponse> page = new PageImpl<>(reportList, PageRequest.of(0, 5), reportList.size());

        given(reportService.getPagedReportsWithFilter(any(Pageable.class), any(), any()))
                .willReturn(page);

        mockMvc.perform(get("/admin/reports/page")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/report"))
                .andExpect(model().attributeExists("reportPage"))
                .andExpect(model().attributeExists("reports"))
                .andExpect(model().attribute("currentPage", 0));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void 관리자는_신고_Fragment_조회할_수_있다() throws Exception {
        ReportResponse report = ReportResponse.builder()
                .id(2L)
                .reportType("댓글")
                .targetId(102L)
                .targetWriterName("임꺽정")
                .reporterName("관리자")
                .reasonType("욕설")
                .reason("기타")
                .createdDate(LocalDateTime.now())
                .boardId(200L)
                .targetMemberId(13L)
                .targetExists(true)
                .suspended(false)
                .targetIsAdmin(false)
                .build();

        Page<ReportResponse> page = new PageImpl<>(List.of(report), PageRequest.of(0, 5), 1);

        given(reportService.getPagedReportsFiltered(any(Pageable.class), any(), any()))
                .willReturn(page);

        mockMvc.perform(get("/admin/reports/reportBody")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/report :: reportBody, pagination"))
                .andExpect(model().attributeExists("reports"))
                .andExpect(model().attributeExists("currentPage"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void 중복_신고시_예외메시지_반환() throws Exception {
        willThrow(new IllegalStateException("이미 신고하셨습니다."))
                .given(reportService)
                .report(eq(ReportType.COMMENT), eq(1L), any(), eq(ReasonType.ETC), any());

        mockMvc.perform(post("/community/reports")
                        .param("type", "COMMENT")
                        .param("targetId", "1")
                        .param("reason", "중복신고")
                        .param("reasonType", "ETC"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("이미 신고하셨습니다."));
    }
}
