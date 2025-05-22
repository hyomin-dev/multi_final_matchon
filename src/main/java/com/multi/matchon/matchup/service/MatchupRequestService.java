package com.multi.matchon.matchup.service;


import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.domain.SportsTypeName;
import com.multi.matchon.common.dto.res.PageResponseDto;
import com.multi.matchon.matchup.domain.MatchupRequest;
import com.multi.matchon.matchup.dto.req.ReqMatchupRequestDto;
import com.multi.matchon.matchup.dto.res.ResMatchupRequestDto;
import com.multi.matchon.matchup.dto.res.ResMatchupRequestListDto;
import com.multi.matchon.matchup.repository.MatchupBoardRepository;
import com.multi.matchon.matchup.repository.MatchupRequestRepository;
import com.multi.matchon.member.domain.Member;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchupRequestService {

    private final MatchupBoardRepository matchupBoardRepository;
    private final MatchupRequestRepository matchupRequestRepository;


    // 등록

    @Transactional
    public void registerMatchupRequest(ReqMatchupRequestDto reqMatchupRequestDto, Member member) {

        Boolean isDuplicate = matchupRequestRepository.isAlreadyRequestedByBoardIdAndMemberId(reqMatchupRequestDto.getBoardId(), member.getId());

        // isDeleted=true ---> result = false, 재요청 가능, 중복 검사 성공
        // status=pending, isDeleted=false  ----> result = true, 재요청 불가능, 중복 검사 성공,
        // status=approved, isDeleted=false ----> result = true, 재요청 불가능, 중복 검사 성공
        // status=denied, isDeleted=false ---> result = false, 재요청 가능, 중복 검사 성공

        if(isDuplicate)
            throw new DuplicateRequestException("matchup 중복된 참가 요청입니다.");
        else{
            MatchupRequest matchupRequest = MatchupRequest.builder()
                    .matchupBoard(matchupBoardRepository.findById(reqMatchupRequestDto.getBoardId()).orElseThrow(()-> new IllegalArgumentException(reqMatchupRequestDto.getBoardId()+"번 게시글은 없습니다.")))
                    .member(member)
                    .selfIntro(reqMatchupRequestDto.getSelfIntro())
                    .participantCount(reqMatchupRequestDto.getParticipantCount())
                    .build();
            matchupRequestRepository.save(matchupRequest);
        }

        //log.info("result = {}",isDuplicate);
    }


    // 조회

    @Transactional(readOnly = true)
    public PageResponseDto<ResMatchupRequestListDto> findAllMyMatchupRequestWithPaging(PageRequest pageRequest, CustomUser user, String sportsType, String date) {

        SportsTypeName sportsTypeName;
        if(sportsType.isBlank())
            sportsTypeName = null;
        else
            sportsTypeName = SportsTypeName.valueOf(sportsType);

        LocalDate matchDate = null;
        if(!date.isBlank())
            matchDate = LocalDate.parse(date);


        Page<ResMatchupRequestListDto> page = matchupRequestRepository.findAllResMatchupRequestListDtosByMemberIdAndSportsTypeAndMatchDateWithPaging(pageRequest,user.getMember().getId(), sportsTypeName, matchDate);

        return PageResponseDto.<ResMatchupRequestListDto>builder()
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
    public ResMatchupRequestDto findResMatchRequestDtoByRequestId(Long requestId) {


        return matchupRequestRepository.findResMatchupRequestDtoByRequestId(requestId).orElseThrow(()->new IllegalArgumentException(requestId+"번 요청은 없습니다."));

    }

    @Transactional(readOnly = true)
    public ReqMatchupRequestDto findReqMatchupRequestDtoByBoardId(Long boardId) {

       return matchupRequestRepository.findReqMatchupRequestDtoByBoardId(boardId).orElseThrow(()->new IllegalArgumentException(boardId+"번 게시글이 업습니다."));
    }

    // 수정

    // 삭제







}
