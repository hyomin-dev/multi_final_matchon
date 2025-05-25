package com.multi.matchon.matchup.service;


import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.domain.SportsTypeName;
import com.multi.matchon.common.domain.Status;
import com.multi.matchon.common.dto.res.PageResponseDto;
import com.multi.matchon.common.exception.custom.CustomException;
import com.multi.matchon.common.exception.custom.hasCanceledMatchRequestMoreThanOnceException;
import com.multi.matchon.common.exception.custom.MatchupRequestLimitExceededException;
import com.multi.matchon.matchup.domain.MatchupRequest;
import com.multi.matchon.matchup.dto.req.ReqMatchupRequestDto;
import com.multi.matchon.matchup.dto.res.ResMatchupRequestDto;
import com.multi.matchon.matchup.dto.res.ResMatchupRequestListDto;
import com.multi.matchon.matchup.repository.MatchupBoardRepository;
import com.multi.matchon.matchup.repository.MatchupRequestRepository;
import com.multi.matchon.member.repository.MemberRepository;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchupRequestService {

    private final MatchupBoardRepository matchupBoardRepository;
    private final MatchupRequestRepository matchupRequestRepository;
    private final MemberRepository memberRepository;


    // 등록

    @Transactional
    public void registerMatchupRequest(ReqMatchupRequestDto reqMatchupRequestDto, CustomUser user) {

      /*  // 1. 2회 이상 취소이력 검사
        Boolean isCanceled = matchupRequestRepository.hasCanceledMatchRequestMoreThanOnce(reqMatchupRequestDto.getBoardId(), user.getMember().getId());
        if(isCanceled)
            throw new hasCanceledMatchRequestMoreThanOnceException("1번의 취소 이력이 있어 Matchup 참가 요청이 불가능합니다.");

        // 2. 중복 검사
        Boolean isDuplicate = matchupRequestRepository.isAlreadyMatchupRequestedByBoardIdAndMemberId(reqMatchupRequestDto.getBoardId(), user.getMember().getId());
        if(isDuplicate)
            throw new DuplicateRequestException("중복된 Matchup 참가 요청입니다.");


        // 3. 재요청이 3번 이상인지 체크(2번까지는 요청 가능)
        Boolean isExceed = matchupRequestRepository.hasExceededTwoMatchupRequestsByBoardIdAndMemberId(reqMatchupRequestDto.getBoardId(), user.getMember().getId());
        if(isExceed)
            throw new MatchupRequestLimitExceededException("Matchup 참가 요청을 2번 하셔서 더 이상 요청은 불가능 합니다.");

         MatchupRequest matchupRequest = MatchupRequest.builder()
                    .matchupBoard(matchupBoardRepository.findById(reqMatchupRequestDto.getBoardId()).orElseThrow(()-> new IllegalArgumentException(reqMatchupRequestDto.getBoardId()+"번 게시글은 없습니다.")))
                    .member(user.getMember())
                    .selfIntro(reqMatchupRequestDto.getSelfIntro())
                    .matchupRequestResubmittedCount(0)
                    .participantCount(reqMatchupRequestDto.getParticipantCount())
                    .build();
            matchupRequestRepository.save(matchupRequest);*/

        // 1. boardId와 applicantId에 대응되는 request가 있는 지 판단

        Optional<MatchupRequest> findMatchupRequestOptional = matchupRequestRepository.findByMatchupBoardIdAndMemberId(reqMatchupRequestDto.getBoardId(), user.getMember().getId());

        if(!findMatchupRequestOptional.isPresent()){
            MatchupRequest newMatchupRequest = MatchupRequest.builder()
                    .matchupBoard(matchupBoardRepository.findByIdAndIsDeletedFalse(reqMatchupRequestDto.getBoardId()).orElseThrow(()->new IllegalArgumentException("Matchup"+reqMatchupRequestDto.getBoardId()+"번 게시글이 존재하지 않습니다.")))
                    .member(memberRepository.findByIdAndIsDeletedFalse(user.getMember().getId()).orElseThrow(()->new IllegalArgumentException("Matchup"+user.getMember().getId()+"번 회원은 없습니다.")))
                    .selfIntro(reqMatchupRequestDto.getSelfIntro())
                    .participantCount(reqMatchupRequestDto.getParticipantCount())
                    .matchupStatus(Status.PENDING)
                    .build();
            matchupRequestRepository.save(newMatchupRequest);
        }
        else{
            MatchupRequest findMatchupRequest = findMatchupRequestOptional.get();

            if(findMatchupRequest.getMatchupStatus()==Status.PENDING && findMatchupRequest.getMatchupRequestSubmittedCount()==1 && findMatchupRequest.getMatchupCancelSubmittedCount()==0 && findMatchupRequest.getIsDeleted()) {
                findMatchupRequest.updateRequestMangementInfo(Status.PENDING, 2, 0, false); // 재요청 1번 상황
            }else if(findMatchupRequest.getMatchupStatus()==Status.DENIED && findMatchupRequest.getMatchupRequestSubmittedCount()==1 && findMatchupRequest.getMatchupCancelSubmittedCount()==0 && findMatchupRequest.getIsDeleted()){
                findMatchupRequest.updateRequestMangementInfo(Status.PENDING, 2, 0, false); // 재요청 2번 상황
            }else if(findMatchupRequest.getMatchupStatus()==Status.DENIED && findMatchupRequest.getMatchupRequestSubmittedCount()==1 && findMatchupRequest.getMatchupCancelSubmittedCount()==0 && !findMatchupRequest.getIsDeleted()){
                findMatchupRequest.updateRequestMangementInfo(Status.PENDING, 2, 0, false); // 재요청 3번 상황
            }else{
                throw new CustomException("Matchup"+"요청할 수 없는 상태입니다. 이전 이력을 확인해주세요.");
            }

        }




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

        return matchupRequestRepository.findResMatchupRequestDtoByRequestId(requestId).orElseThrow(()->new IllegalArgumentException("Matchup"+requestId+"번 요청은 없습니다."));

    }

    @Transactional(readOnly = true)
    public ReqMatchupRequestDto findReqMatchupRequestDtoByBoardId(Long boardId) {

       return matchupBoardRepository.findReqMatchupRequestDtoByBoardId(boardId).orElseThrow(()->new IllegalArgumentException("Matchup"+boardId+"번 게시글이 없습니다."));
    }


//    @Transactional
//    public void updateMatchupRequest(ResMatchupRequestDto resMatchupRequestDto, Long requestId) {
//        MatchupRequest findMatchupRequest = matchupRequestRepository.findById(requestId).orElseThrow(()->new IllegalArgumentException(requestId+"번 요청글이 없습니다."));
//
//        findMatchupRequest.update(resMatchupRequestDto.getSelfIntro(), resMatchupRequestDto.getParticipantCount());
//
//    }



    // 수정

    // 삭제







}
