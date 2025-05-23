package com.multi.matchon.matchup.service;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.domain.Attachment;
import com.multi.matchon.common.domain.BoardType;
import com.multi.matchon.common.domain.SportsTypeName;
import com.multi.matchon.common.dto.res.PageResponseDto;
import com.multi.matchon.common.repository.AttachmentRepository;
import com.multi.matchon.common.repository.SportsTypeRepository;
import com.multi.matchon.matchup.domain.MatchupBoard;
import com.multi.matchon.matchup.domain.MatchupRequest;
import com.multi.matchon.matchup.dto.req.ReqMatchupBoardDto;
import com.multi.matchon.matchup.dto.req.ReqMatchupRequestDto;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardDto;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardListDto;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

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

        MatchupBoard newMatchupBoard = MatchupBoard.builder()
                .member(user.getMember())
                .sportsType(sportsTypeRepository.findBySportsTypeName(SportsTypeName.valueOf(reqMatchupBoardDto.getSportsTypeName())).orElseThrow(()-> new IllegalArgumentException(reqMatchupBoardDto.getSportsTypeName()+"는 Matchup에서 지원하지 않는 종목입니다.")))
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
    public ResMatchupBoardDto findMatchupBoardByBoardId(Long boardId) {

        MatchupBoard matchupBoard = matchupBoardRepository.findMatchupBoardByBoardId(boardId).orElseThrow(()->new IllegalArgumentException(boardId +"번 Matchup 게시글이 존재하지 않습니다."));

        List<Attachment> attachments = attachmentRepository.findAllByBoardTypeAndBoardNumber(BoardType.MATCHUP_BOARD, boardId);

        if(attachments.isEmpty()&&matchupBoard.getReservationAttachmentEnabled())
            throw new IllegalArgumentException(boardId +"번 Matchup 게시글의 첨부파일이 존재해야하는데 없습니다.");

        return ResMatchupBoardDto.builder()
                .boardId(matchupBoard.getId())
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
                .myMannerTemperature(matchupBoard.getMember().getMyTemperature())
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
    public void updateBoard(ResMatchupBoardDto resMatchupBoardDto) {
        MatchupBoard findMatchupBoard = matchupBoardRepository.findMatchupBoardByBoardIdAndIsDeleted(resMatchupBoardDto.getBoardId()).orElseThrow(()->new IllegalArgumentException(resMatchupBoardDto.getBoardId()+"번 게시글이 없습니다."));

        findMatchupBoard.update(
                sportsTypeRepository.findBySportsTypeName(resMatchupBoardDto.getSportsTypeName()).orElseThrow(()-> new IllegalArgumentException(resMatchupBoardDto.getSportsTypeName()+"는 Matchup에서 지원하지 않는 종목입니다.")),
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
        System.out.println("Tt");

        if(!Objects.requireNonNull(resMatchupBoardDto.getReservationFile().getOriginalFilename()).isBlank()){
            matchupService.updateFile(resMatchupBoardDto.getReservationFile(), findMatchupBoard);
        }
    }

    // 삭제

    @Transactional
    public void softDeleteMatchupBoard(Long boardId) {
        MatchupBoard findMatchupBoard = matchupBoardRepository.findMatchupBoardByBoardIdAndIsDeleted(boardId).orElseThrow(()->new IllegalArgumentException(boardId+"번 Matchup 게시글이 없습니다."));

        findMatchupBoard.delete(true);

        matchupBoardRepository.save(findMatchupBoard);

        List<Attachment> findAttachments = attachmentRepository.findAllByBoardTypeAndBoardNumber(BoardType.MATCHUP_BOARD, boardId);
        if(findAttachments.isEmpty())
            throw new IllegalArgumentException(BoardType.MATCHUP_BOARD+"타입, "+findMatchupBoard.getId()+"번에는 첨부파일이 없습니다. Matchup ");
        findAttachments.get(0).delete(true);

        attachmentRepository.save(findAttachments.get(0));

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
