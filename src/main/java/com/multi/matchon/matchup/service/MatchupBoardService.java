package com.multi.matchon.matchup.service;

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

    // 등록

    @Transactional
    public void registerMatchupBoard(ReqMatchupBoardDto reqMatchupBoardDto, CustomUser user) {



        Long numberOfTodayMatchupBoards = matchupBoardRepository.countTodayMatchupBoards(user.getMember().getId(), LocalDateTime.now().minusHours(24));
        if(numberOfTodayMatchupBoards>=3){
            throw new CustomException("Matchup 게시글은 하루에 3번만 작성할 수 있습니다.");
        }

        MatchupBoard newMatchupBoard = MatchupBoard.builder()
                .member(user.getMember())
                .sportsType(sportsTypeRepository.findBySportsTypeName(SportsTypeName.valueOf(reqMatchupBoardDto.getSportsTypeName())).orElseThrow(()-> new IllegalArgumentException("Matchup"+reqMatchupBoardDto.getSportsTypeName()+"는 에서 지원하지 않는 종목입니다.")))
                .reservationAttachmentEnabled(true)
                .teamIntro(reqMatchupBoardDto.getTeamIntro())
                .sportsFacilityName(reqMatchupBoardDto.getSportsFacilityName())
                .sportsFacilityAddress(reqMatchupBoardDto.getSportsFacilityAddress())
                .matchDatetime(reqMatchupBoardDto.getMatchDateTime())
                .matchDuration(LocalTime.of(reqMatchupBoardDto.getMatchDuration()/60,reqMatchupBoardDto.getMatchDuration()%60))
                .currentParticipantCount(reqMatchupBoardDto.getCurrentParticipantsCount())
                .maxParticipants(reqMatchupBoardDto.getMaxParticipants())
                .minMannerTemperature(reqMatchupBoardDto.getMinMannerTemperature())
                .matchDescription(reqMatchupBoardDto.getMatchDescription())
                .build();
        MatchupBoard matchupBoard = matchupBoardRepository.save(newMatchupBoard);
        matchupService.insertFile(reqMatchupBoardDto.getReservationFile(), matchupBoard);
    }

    // 조회

    @Transactional(readOnly = true)
    public ResMatchupBoardDto findMatchupBoardByBoardId(Long boardId, CustomUser user) {

        MatchupBoard matchupBoard = matchupBoardRepository.findMatchupBoardByBoardId(boardId).orElseThrow(()->new IllegalArgumentException("Matchup"+boardId +"번 게시글이 존재하지 않습니다."));

        List<Attachment> attachments = attachmentRepository.findAllByBoardTypeAndBoardNumber(BoardType.MATCHUP_BOARD, boardId);

        if(attachments.isEmpty()&&matchupBoard.getReservationAttachmentEnabled())
            throw new IllegalArgumentException("Matchup"+boardId +"번 게시글의 첨부파일이 존재해야하는데 없습니다.");

        return ResMatchupBoardDto.builder()
                .boardId(matchupBoard.getId())
                .memberId(matchupBoard.getMember().getId())
                .memberEmail(matchupBoard.getMember().getMemberEmail())
                .memberName(matchupBoard.getMember().getMemberName())
                .teamName(matchupBoard.getMember().getTeam().getTeamName())
                .teamIntro(matchupBoard.getTeamIntro())
                .sportsTypeName(matchupBoard.getSportsType().getSportsTypeName())
                .sportsFacilityName(matchupBoard.getSportsFacilityName())
                .sportsFacilityAddress(matchupBoard.getSportsFacilityAddress())
                .matchDatetime(matchupBoard.getMatchDatetime())
                .matchDuration(matchupBoard.getMatchDuration())
                .currentParticipantCount(matchupBoard.getCurrentParticipantCount())
                .maxParticipants(matchupBoard.getMaxParticipants())
                .minMannerTemperature(matchupBoard.getMinMannerTemperature())
                .myMannerTemperature(user.getMember().getMyTemperature()) //matchupBoard.getMember().getMyTemperature()
                .matchDescription(matchupBoard.getMatchDescription())
                .originalName(attachments.get(0).getOriginalName())
                .savedName(attachments.get(0).getSavedName())
                .savedPath(attachments.get(0).getSavePath())
                .build();

    }

    @Transactional(readOnly = true)
    public PageResponseDto<ResMatchupBoardListDto> findAllMatchupBoardsWithPaging(PageRequest pageRequest, String sportsType, String region, String date  ) {
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

        Page<ResMatchupBoardListDto> page = matchupBoardRepository.findAllMatchupBoardsWithPaging(pageRequest, sportsTypeName, region, matchDate);
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

    @Transactional(readOnly = true)
    public PageResponseDto<ResMatchupBoardListDto> findAllMyMatchupBoardsWithPaging(PageRequest pageRequest, CustomUser user, String sportsType, String date ) {
        SportsTypeName sportsTypeName;
        if(sportsType.isBlank())
            sportsTypeName = null;
        else
            sportsTypeName = SportsTypeName.valueOf(sportsType);

        LocalDate matchDate = null;
        if(!date.isBlank())
            matchDate = LocalDate.parse(date);


        Page<ResMatchupBoardListDto> page = matchupBoardRepository.findAllResMatchupBoardListDtosByMemberEmailWithPaging(pageRequest, user.getMember().getMemberEmail(), sportsTypeName, matchDate);
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

    @Transactional
    public void updateBoard(ResMatchupBoardDto resMatchupBoardDto, CustomUser user) {
        MatchupBoard findMatchupBoard = matchupBoardRepository.findMatchupBoardByBoardIdAndIsDeleted(resMatchupBoardDto.getBoardId()).orElseThrow(()->new IllegalArgumentException("Matchup"+resMatchupBoardDto.getBoardId()+"번 게시글이 없습니다."));


//        if(resMatchupBoardDto.getTeamIntro()==null)
//            throw new CustomException("Matchup 소속 팀이 있어야 합니다.");
//        else if(resMatchupBoardDto.getSportsFacilityName() ==null)
//            throw new CustomException("Matchup 경기장 명을 입력하세요.");
//        else if(resMatchupBoardDto.getSportsFacilityAddress() == null)
//            throw new CustomException("Matchup 경기장 주소를 입력하세요.");
//        else if(resMatchupBoardDto.getMatchDatetime() == null)
//            throw new CustomException("")

        if(findMatchupBoard.getMatchDatetime().isBefore(LocalDateTime.now()))
            throw new CustomException("Matchup 경기 시작 시간이 지나 수정할 수 없습니다.");

        if(resMatchupBoardDto.getMatchDatetime().isBefore(LocalDateTime.now()))
            throw new CustomException("Matchup 경기 시작 시간은 현재 시간 이후만 가능합니다.");

        if(findMatchupBoard.getCurrentParticipantCount()>resMatchupBoardDto.getMaxParticipants())
            throw new CustomException("Matchup 총 모집 인원은 현재 모집된 인원 이상이여야 합니다.");

        if(resMatchupBoardDto.getMinMannerTemperature()>user.getMember().getMyTemperature())
            throw new CustomException("Matchup 하한 매너 온도는 내 매너온도 이상이어야 합니다.");

        findMatchupBoard.update(
                sportsTypeRepository.findBySportsTypeName(resMatchupBoardDto.getSportsTypeName()).orElseThrow(()-> new IllegalArgumentException("Matchup"+resMatchupBoardDto.getSportsTypeName()+"는 에서 지원하지 않는 종목입니다.")),
                resMatchupBoardDto.getTeamIntro(),
                resMatchupBoardDto.getSportsFacilityName(),
                resMatchupBoardDto.getSportsFacilityAddress(),
                resMatchupBoardDto.getMatchDatetime(),
                resMatchupBoardDto.getMatchDuration(),
                resMatchupBoardDto.getCurrentParticipantCount(),
                resMatchupBoardDto.getMaxParticipants(),
                resMatchupBoardDto.getMinMannerTemperature(),
                resMatchupBoardDto.getMatchDescription()
        );
        matchupBoardRepository.save(findMatchupBoard);

        //if(resMatchupBoardDto.getReservationFile())
        //System.out.println("Tt");

        if(!Objects.requireNonNull(resMatchupBoardDto.getReservationFile().getOriginalFilename()).isBlank()){
            matchupService.updateFile(resMatchupBoardDto.getReservationFile(), findMatchupBoard);
        }
    }

    // 삭제

    @Transactional
    public void softDeleteMatchupBoard(Long boardId) {
        MatchupBoard findMatchupBoard = matchupBoardRepository.findMatchupBoardByBoardIdAndIsDeleted(boardId).orElseThrow(()->new IllegalArgumentException("Matchup"+boardId+"번 게시글이 없습니다."));

        findMatchupBoard.delete(true);

        matchupBoardRepository.save(findMatchupBoard);

        List<Attachment> findAttachments = attachmentRepository.findAllByBoardTypeAndBoardNumber(BoardType.MATCHUP_BOARD, boardId);
        if(findAttachments.isEmpty())
            throw new IllegalArgumentException("Matchup"+BoardType.MATCHUP_BOARD+"타입, "+findMatchupBoard.getId()+"번에는 첨부파일이 없습니다.");
        findAttachments.get(0).delete(true);

        attachmentRepository.save(findAttachments.get(0));

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
