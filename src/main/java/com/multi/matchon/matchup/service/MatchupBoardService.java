package com.multi.matchon.matchup.service;

import com.multi.matchon.chat.domain.ChatRoom;
import com.multi.matchon.chat.service.ChatService;
import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.domain.Attachment;
import com.multi.matchon.common.domain.BoardType;
import com.multi.matchon.common.domain.SportsTypeName;
import com.multi.matchon.common.dto.res.PageResponseDto;
import com.multi.matchon.common.exception.custom.CustomException;
import com.multi.matchon.common.repository.AttachmentRepository;
import com.multi.matchon.common.repository.SportsTypeRepository;
import com.multi.matchon.matchup.domain.MatchupBoard;
import com.multi.matchon.matchup.domain.MatchupRequest;
import com.multi.matchon.matchup.dto.req.ReqMatchupBoardDto;
import com.multi.matchon.matchup.dto.req.ReqMatchupBoardEditDto;
import com.multi.matchon.matchup.dto.req.ReqMatchupRequestDto;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardDto;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardListDto;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardOverviewDto;
import com.multi.matchon.matchup.repository.MatchupBoardRepository;
import com.multi.matchon.member.domain.Member;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchupBoardService {

    private final SportsTypeRepository sportsTypeRepository;

    private final MatchupBoardRepository matchupBoardRepository;
    private final MatchupService matchupService;
    private final AttachmentRepository attachmentRepository;
    private final ChatService chatService;

    // 등록


    /*
    * Matchup 게시글 작성한 내용을 서버에 저장하는 메서드
    * group chat 생성
    * */
    @Transactional
    public void registerMatchupBoard(ReqMatchupBoardDto reqMatchupBoardDto, CustomUser user) {


        // 게시글을 24시간에 3번만 작성할 수 있도록 검사
        Long numberOfTodayMatchupBoards = matchupBoardRepository.countTodayMatchupBoards(user.getMember().getId(), LocalDateTime.now().minusHours(24));
        if(numberOfTodayMatchupBoards>=3){
            throw new CustomException("Matchup 게시글은 하루에 3번만 작성할 수 있습니다.");
        }

        // Matchup Board 생성하면서 group chat 생성
        MatchupBoard newMatchupBoard = MatchupBoard.builder()
                .writer(user.getMember())
                .sportsType(sportsTypeRepository.findBySportsTypeName(SportsTypeName.valueOf(reqMatchupBoardDto.getSportsTypeName())).orElseThrow(()-> new IllegalArgumentException("Matchup"+reqMatchupBoardDto.getSportsTypeName()+"는 에서 지원하지 않는 종목입니다.")))
                .reservationAttachmentEnabled(true)
                .teamIntro(reqMatchupBoardDto.getTeamIntro())
                .sportsFacilityName(reqMatchupBoardDto.getSportsFacilityName())
                .sportsFacilityAddress(reqMatchupBoardDto.getSportsFacilityAddress())
                .matchDatetime(reqMatchupBoardDto.getMatchDatetime())
                .matchDuration(LocalTime.of(reqMatchupBoardDto.getMatchDuration()/60,reqMatchupBoardDto.getMatchDuration()%60))
                .currentParticipantCount(reqMatchupBoardDto.getCurrentParticipantCount())
                .maxParticipants(reqMatchupBoardDto.getMaxParticipants())
                .minMannerTemperature(reqMatchupBoardDto.getMinMannerTemperature())
                .matchDescription(reqMatchupBoardDto.getMatchDescription())
                .chatRoom(chatService.registerGroupChatRoom(user.getMember()))
                .build();
        MatchupBoard matchupBoard = matchupBoardRepository.save(newMatchupBoard);
        matchupService.insertFile(reqMatchupBoardDto.getReservationFile(), matchupBoard);
    }

    // 조회


    /*
    * Matchup 게시글 상세조회 페이지로 나갈 정보
    * +
    * Matchup 게시글 수정하기 페이지로 나갈 정보
    * */
    @Transactional(readOnly = true)
    public ResMatchupBoardDto findMatchupBoardByBoardId(Long boardId) {

        MatchupBoard matchupBoard = matchupBoardRepository.findMatchupBoardByBoardId(boardId).orElseThrow(()->new IllegalArgumentException("Matchup"+boardId +"번 게시글이 존재하지 않습니다."));

        List<Attachment> attachments = attachmentRepository.findAllByBoardTypeAndBoardNumber(BoardType.MATCHUP_BOARD, boardId);

        if(attachments.isEmpty()&&matchupBoard.getReservationAttachmentEnabled())
            throw new IllegalArgumentException("Matchup"+boardId +"번 게시글의 첨부파일이 존재해야하는데 없습니다.");

        return ResMatchupBoardDto.builder()
                .boardId(matchupBoard.getId())
                .writerId(matchupBoard.getWriter().getId())
                .writerEmail(matchupBoard.getWriter().getMemberEmail())
                .writerName(matchupBoard.getWriter().getMemberName())
                .teamName(matchupBoard.getWriter().getTeam().getTeamName())
                .teamIntro(matchupBoard.getTeamIntro())
                .sportsTypeName(matchupBoard.getSportsType().getSportsTypeName())
                .sportsFacilityName(matchupBoard.getSportsFacilityName())
                .sportsFacilityAddress(matchupBoard.getSportsFacilityAddress())
                .matchDatetime(matchupBoard.getMatchDatetime())
                .matchDuration(matchupBoard.getMatchDuration())
                .currentParticipantCount(matchupBoard.getCurrentParticipantCount())
                .maxParticipants(matchupBoard.getMaxParticipants())
                .minMannerTemperature(matchupBoard.getMinMannerTemperature())
                .myMannerTemperature(matchupBoard.getWriter().getMyTemperature()) //matchupBoard.getMember().getMyTemperature()
                .matchDescription(matchupBoard.getMatchDescription())
                .originalName(attachments.get(0).getOriginalName())
                .savedName(attachments.get(0).getSavedName())
                .savedPath(attachments.get(0).getSavePath())
                .build();

    }


    /*
    * 전체 Matchup 게시글 목록을 가져오는 메서드
    * */
    @Transactional(readOnly = true)
    public PageResponseDto<ResMatchupBoardListDto> findAllMatchupBoardsWithPaging(PageRequest pageRequest, String sportsType, String region, String date, Boolean availableFilter, CustomUser user) {
        SportsTypeName sportsTypeName;
        if(sportsType.isBlank())
            sportsTypeName = null;
        else
            sportsTypeName = SportsTypeName.valueOf(sportsType);

        if(region.isBlank())
            region = null;

        LocalDate matchDate = null;
        if(!date.isBlank())
            matchDate = LocalDate.parse(date);

        Page<ResMatchupBoardListDto> page = matchupBoardRepository.findAllMatchupBoardsWithPaging(pageRequest, sportsTypeName, region, matchDate, availableFilter, user.getMember().getMyTemperature());
        return PageResponseDto.<ResMatchupBoardListDto>builder()
                .items(page.getContent())
                .pageInfo(PageResponseDto.PageInfoDto.builder()
                        .page(page.getNumber())
                        .size(page.getNumberOfElements())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .isFirst(page.isFirst())
                        .isLast(page.isLast())
                        .build())
                .build();
    }

    /*
     * 내가 작성한 Matchup 게시글 목록을 가져오는 메서드
     * */
    @Transactional(readOnly = true)
    public PageResponseDto<ResMatchupBoardListDto> findAllMyMatchupBoardsWithPaging(PageRequest pageRequest, CustomUser user, String sportsType, String date, Boolean availableFilter ) {
        SportsTypeName sportsTypeName;
        if(sportsType.isBlank())
            sportsTypeName = null;
        else
            sportsTypeName = SportsTypeName.valueOf(sportsType);

        LocalDate matchDate = null;
        if(!date.isBlank())
            matchDate = LocalDate.parse(date);


        Page<ResMatchupBoardListDto> page = matchupBoardRepository.findAllResMatchupBoardListDtosByMemberWithPaging(pageRequest, user.getMember(), sportsTypeName, matchDate, availableFilter);
        return PageResponseDto.<ResMatchupBoardListDto>builder()
                .items(page.getContent())
                .pageInfo(PageResponseDto.PageInfoDto.builder()
                        .page(page.getNumber())
                        .size(page.getNumberOfElements())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .isFirst(page.isFirst())
                        .isLast(page.isLast())
                        .build())
                .build();
    }

    // 수정


    /*
    * matchup board 수정
    * */
    @Transactional
    public void updateBoard(ReqMatchupBoardEditDto reqMatchupBoardEditDto, CustomUser user) {
        MatchupBoard findMatchupBoard = matchupBoardRepository.findMatchupBoardByBoardIdAndIsDeleted(reqMatchupBoardEditDto.getBoardId()).orElseThrow(()->new IllegalArgumentException("Matchup"+reqMatchupBoardEditDto.getBoardId()+"번 게시글이 없습니다."));

        if(findMatchupBoard.getMatchDatetime().isBefore(LocalDateTime.now()))
            throw new CustomException("Matchup 경기 시작 시간이 지나 수정할 수 없습니다.");

        if(reqMatchupBoardEditDto.getMatchDatetime().isBefore(LocalDateTime.now()))
            throw new CustomException("Matchup 경기 시작 시간은 현재 시간 이후만 가능합니다.");

        if(findMatchupBoard.getCurrentParticipantCount()>reqMatchupBoardEditDto.getMaxParticipants())
            throw new CustomException("Matchup 총 모집 인원은 현재 모집된 인원 이상이여야 합니다.");

        if(reqMatchupBoardEditDto.getMinMannerTemperature()>reqMatchupBoardEditDto.getMyMannerTemperature())
            throw new CustomException("Matchup 하한 매너 온도는 내 매너온도 이상이어야 합니다.");

        findMatchupBoard.update(
                sportsTypeRepository.findBySportsTypeName(SportsTypeName.valueOf(reqMatchupBoardEditDto.getSportsTypeName())).orElseThrow(()-> new IllegalArgumentException("Matchup"+reqMatchupBoardEditDto.getSportsTypeName()+"는 에서 지원하지 않는 종목입니다.")),
                reqMatchupBoardEditDto.getTeamIntro(),
                reqMatchupBoardEditDto.getSportsFacilityName(),
                reqMatchupBoardEditDto.getSportsFacilityAddress(),
                reqMatchupBoardEditDto.getMatchDatetime(),
                reqMatchupBoardEditDto.getMatchDuration(),
                reqMatchupBoardEditDto.getCurrentParticipantCount(),
                reqMatchupBoardEditDto.getMaxParticipants(),
                reqMatchupBoardEditDto.getMinMannerTemperature(),
                reqMatchupBoardEditDto.getMatchDescription()
        );

        if(!Objects.requireNonNull(reqMatchupBoardEditDto.getReservationFile().getOriginalFilename()).isBlank()){
            matchupService.updateFile(reqMatchupBoardEditDto.getReservationFile(), findMatchupBoard);
        }
    }

    // 삭제


    /*
    * Matchup 게시글 삭제하기
    * */
    @Transactional
    public void softDeleteMatchupBoard(Long boardId) {
        MatchupBoard findMatchupBoard = matchupBoardRepository.findMatchupBoardByBoardIdAndIsDeleted(boardId).orElseThrow(()->new IllegalArgumentException("Matchup"+boardId+"번 게시글이 없습니다."));

        if(findMatchupBoard.getMatchDatetime().isBefore(LocalDateTime.now()))
            throw new CustomException("Matchup 경기 시작 시간이 지나 삭제할 수 없습니다.");


        findMatchupBoard.delete(true);

        List<Attachment> findAttachments = attachmentRepository.findAllByBoardTypeAndBoardNumber(BoardType.MATCHUP_BOARD, boardId);
        if(findAttachments.isEmpty())
            throw new IllegalArgumentException("Matchup"+BoardType.MATCHUP_BOARD+"타입, "+findMatchupBoard.getId()+"번에는 첨부파일이 없습니다.");
        findAttachments.get(0).delete(true);

    }

    @Transactional(readOnly = true)
    public ResMatchupBoardOverviewDto findResMatchupOverviewDto(Long boardId) {

        return matchupBoardRepository.findResMatchupOverviewDto(boardId).orElseThrow(()->new IllegalArgumentException("Matchup"+boardId+"번 게시글이 없습니다."));
    }
    // ========================================================================================================
    //                                                    테스트 해본 코드
    // ========================================================================================================

    //    public List<MatchupBoard> findAll() {
//        List<MatchupBoard> matchupBoards = matchupBoardRepository.findAll();
//        List<MatchupBoard> matchupBoards1 = matchupBoardRepository.findAllWithMember();
//        List<MatchupBoard> matchupBoards2 = matchupBoardRepository.findAllWithMemberAndWithSportsType();
//
//        return matchupBoards;
//    }




}
