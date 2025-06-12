package com.multi.matchon.community.service;

import com.multi.matchon.community.domain.*;
import com.multi.matchon.community.dto.res.ReportResponse;
import com.multi.matchon.community.repository.BoardRepository;
import com.multi.matchon.community.repository.CommentRepository;
import com.multi.matchon.community.repository.ReportRepository;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.domain.MemberRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ReportServiceTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private CommentRepository commentRepository;

    private Member 신고자;
    private Member 대상자;
    private Board 게시글;
    private Comment 댓글;

    @BeforeEach
    void setUp() {
        신고자 = Member.builder().id(1L).memberName("신고자").memberRole(MemberRole.USER).build();
        대상자 = Member.builder().id(2L).memberName("대상자").memberRole(MemberRole.USER).build();
        게시글 = Board.builder().id(100L).member(대상자).build();
        댓글 = Comment.builder().id(200L).member(대상자).board(게시글).build();
    }

    @Test
    void 게시글에_대한_신고가_정상적으로_저장된다() {
        when(reportRepository.findByReportTypeAndTargetIdAndReporter(ReportType.BOARD, 100L, 신고자))
                .thenReturn(Optional.empty());
        when(boardRepository.findById(100L)).thenReturn(Optional.of(게시글));

        reportService.report(ReportType.BOARD, 100L, "부적절한 내용", ReasonType.SPAM, 신고자);

        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void 댓글에_대한_중복_신고시_예외발생() {
        when(reportRepository.findByReportTypeAndTargetIdAndReporter(ReportType.COMMENT, 200L, 신고자))
                .thenReturn(Optional.of(new Report()));

        assertThatThrownBy(() -> reportService.report(ReportType.COMMENT, 200L, "욕설", ReasonType.ETC, 신고자))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 신고한 대상입니다.");
    }

    @Test
    void 전체_신고목록_조회시_응답형태로_변환된다() {
        Report report = Report.builder()
                .id(1L)
                .reportType(ReportType.BOARD)
                .targetId(100L)
                .reason("부적절")
                .reasonType(ReasonType.SPAM)
                .reporter(신고자)
                .build();

        when(reportRepository.findAll(any(Sort.class))).thenReturn(List.of(report));
        when(boardRepository.findById(100L)).thenReturn(Optional.of(게시글));

        List<ReportResponse> results = reportService.getAllReports();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTargetWriterName()).isEqualTo("대상자");
    }

    @Test
    void 페이징된_신고목록_조회_성공() {
        Report report = Report.builder()
                .id(1L)
                .reportType(ReportType.BOARD)
                .targetId(100L)
                .reason("신고내용")
                .reasonType(ReasonType.SPAM)
                .reporter(신고자)
                .build();

        Page<Report> reportPage = new PageImpl<>(List.of(report));
        when(reportRepository.findAll(any(Pageable.class))).thenReturn(reportPage);
        when(boardRepository.findById(100L)).thenReturn(Optional.of(게시글));

        Page<ReportResponse> result = reportService.getPagedReports(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTargetWriterName()).isEqualTo("대상자");
    }

    @Test
    void 필터_조건으로_신고목록_조회_성공() {
        Report report = Report.builder()
                .id(1L)
                .reportType(ReportType.BOARD)
                .targetId(100L)
                .reasonType(ReasonType.SPAM)
                .reporter(신고자)
                .build();

        Page<Report> reportPage = new PageImpl<>(List.of(report));

        when(reportRepository.findByReportTypeAndReasonType(ReportType.BOARD, ReasonType.SPAM, PageRequest.of(0, 5)))
                .thenReturn(reportPage);
        when(boardRepository.findById(100L)).thenReturn(Optional.of(게시글));

        Page<ReportResponse> result = reportService.getPagedReportsWithFilter(
                PageRequest.of(0, 5), ReportType.BOARD, ReasonType.SPAM);

        assertThat(result.getContent().get(0).getReportType()).isEqualTo("BOARD");
    }
}
