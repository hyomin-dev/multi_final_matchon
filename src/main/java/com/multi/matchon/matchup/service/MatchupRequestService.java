package com.multi.matchon.matchup.service;


import com.multi.matchon.chat.service.ChatService;
import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.domain.SportsTypeName;
import com.multi.matchon.common.domain.Status;
import com.multi.matchon.common.dto.res.PageResponseDto;
import com.multi.matchon.common.exception.custom.CustomException;
import com.multi.matchon.common.exception.custom.hasCanceledMatchRequestMoreThanOnceException;
import com.multi.matchon.common.exception.custom.MatchupRequestLimitExceededException;
import com.multi.matchon.matchup.domain.MatchupBoard;
import com.multi.matchon.matchup.domain.MatchupRequest;
import com.multi.matchon.matchup.dto.req.ReqMatchupRequestDto;
import com.multi.matchon.matchup.dto.req.ReqMatchupRequestEditDto;
import com.multi.matchon.matchup.dto.res.ResMatchupRequestDto;
import com.multi.matchon.matchup.dto.res.ResMatchupRequestListDto;
import com.multi.matchon.matchup.dto.res.ResMatchupRequestOverviewListDto;
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
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchupRequestService {

    private final MatchupBoardRepository matchupBoardRepository;
    private final MatchupRequestRepository matchupRequestRepository;
    private final MemberRepository memberRepository;
    private final ChatService chatService;


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


        // 1. 참가 요청할 게시물이 있는지 확인

        MatchupBoard findMatchupBoard = matchupBoardRepository.findByIdAndIsDeletedFalse(reqMatchupRequestDto.getBoardId()).orElseThrow(()->new IllegalArgumentException("Matchup"+reqMatchupRequestDto.getBoardId()+"번 게시글이 존재하지 않습니다."));

        if(findMatchupBoard.getMatchDatetime().isBefore(LocalDateTime.now())){
            throw new CustomException("Matchup 경기 시작 시간이 지나 참가 요청할 수 없습니다.");
        }

        if(findMatchupBoard.getCurrentParticipantCount()>findMatchupBoard.getMaxParticipants()){
            throw new CustomException("Matchup 현재 모집 인원을 초과해서 참가 요청을 할 수 없습니다.");
        }

        if(findMatchupBoard.getCurrentParticipantCount()+reqMatchupRequestDto.getParticipantCount()>findMatchupBoard.getMaxParticipants()){
            throw new CustomException("Matchup 신청 하신 인원은 현재 모집원을 초과해서 신청할 수 없습니다.");
        }

        // 2. boardId와 applicantId에 대응되는 request가 있는 지 판단

        Optional<MatchupRequest> findMatchupRequestOptional = matchupRequestRepository.findByMatchupBoardIdAndApplicantId(reqMatchupRequestDto.getBoardId(), user.getMember().getId());

        if(findMatchupRequestOptional.isEmpty()){
            MatchupRequest newMatchupRequest = MatchupRequest.builder()
                    .matchupBoard(findMatchupBoard)
                    .member(user.getMember()) //applicant
                    .selfIntro(reqMatchupRequestDto.getSelfIntro())
                    .participantCount(reqMatchupRequestDto.getParticipantCount())
                    .matchupStatus(Status.PENDING)
                    .build();
            matchupRequestRepository.save(newMatchupRequest);
        }
        else{
//            MatchupRequest findMatchupRequest = findMatchupRequestOptional.get();
//
//            if(findMatchupRequest.getMatchupStatus()==Status.PENDING && findMatchupRequest.getMatchupRequestSubmittedCount()==1 && findMatchupRequest.getMatchupCancelSubmittedCount()==0 && findMatchupRequest.getIsDeleted()) {
//                findMatchupRequest.updateRequestMangementInfo(Status.PENDING, 2, 0, false); // 재요청 1번 상황
//            }else if(findMatchupRequest.getMatchupStatus()==Status.DENIED && findMatchupRequest.getMatchupRequestSubmittedCount()==1 && findMatchupRequest.getMatchupCancelSubmittedCount()==0 && findMatchupRequest.getIsDeleted()){
//                findMatchupRequest.updateRequestMangementInfo(Status.PENDING, 2, 0, false); // 재요청 2번 상황
//            }else if(findMatchupRequest.getMatchupStatus()==Status.DENIED && findMatchupRequest.getMatchupRequestSubmittedCount()==1 && findMatchupRequest.getMatchupCancelSubmittedCount()==0 && !findMatchupRequest.getIsDeleted()){
//                findMatchupRequest.updateRequestMangementInfo(Status.PENDING, 2, 0, false); // 재요청 3번 상황
//            }
//            else{
                throw new CustomException("Matchup 요청 이력이 있어 현재 참가 요청을 할 수 없습니다.");
//            }
        }
    }

    // 조회

    @Transactional(readOnly = true)
    public PageResponseDto<ResMatchupRequestListDto> findAllMyMatchupRequestWithPaging(PageRequest pageRequest, CustomUser user, String sportsType, String date, Boolean availableFilter) {

        SportsTypeName sportsTypeName;
        if(sportsType.isBlank())
            sportsTypeName = null;
        else
            sportsTypeName = SportsTypeName.valueOf(sportsType);

        LocalDate matchDate = null;
        if(!date.isBlank())
            matchDate = LocalDate.parse(date);


        Page<ResMatchupRequestListDto> page = matchupRequestRepository.findAllResMatchupRequestListDtosByMemberIdAndSportsTypeAndMatchDateWithPaging(pageRequest,user.getMember().getId(), sportsTypeName, matchDate, availableFilter);

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

    // 참가요청 등록시 사용, board찾음
    @Transactional(readOnly = true)
    public ReqMatchupRequestDto findReqMatchupRequestDtoByBoardId(Long boardId) {

        ReqMatchupRequestDto reqMatchupRequestDto =  matchupBoardRepository.findReqMatchupRequestDtoByBoardId(boardId).orElseThrow(()->new IllegalArgumentException("Matchup"+boardId+"번 게시글이 없습니다."));

        // 경기 시작 시간이 지났는지 확인
        if(reqMatchupRequestDto.getMatchDatetime().isBefore(LocalDateTime.now())){
            throw new CustomException("Matchup 경기 시작 시간이 지나 참가 요청할 수 없습니다.");
        }

        return reqMatchupRequestDto;
    }

    // 상세보기용
    @Transactional(readOnly = true)
    public ResMatchupRequestDto findResMatchRequestDtoByRequestId(Long requestId) {

        return matchupRequestRepository.findResMatchupRequestDtoByRequestId(requestId).orElseThrow(()->new IllegalArgumentException("Matchup"+requestId+"번 요청은 없습니다."));

    }

    public PageResponseDto<ResMatchupRequestOverviewListDto> findAllMatchupRequestByBoardWithPaging(PageRequest pageRequest, Long boardId) {

        Page<ResMatchupRequestOverviewListDto> page = matchupRequestRepository.findAllResMatchupRequestOverviewListDtoByBoardIdAndSportsTypeWithPaging(pageRequest,boardId);

        return PageResponseDto.<ResMatchupRequestOverviewListDto>builder()
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


    //수정하기 조회용, 상태 체크를 해야됨
    @Transactional(readOnly = true)
    public ResMatchupRequestDto findResMatchRequestDtoByRequestIdAndModifyStatus(Long requestId) {

        ResMatchupRequestDto resMatchupRequestDto = matchupRequestRepository.findResMatchupRequestDtoByRequestId(requestId).orElseThrow(()->new IllegalArgumentException("Matchup"+requestId+"번 요청은 없습니다."));

        // 0. 모집된 인원이 총 모집 인원보다 크거나 같은 경우 예외 발생
        if(resMatchupRequestDto.getCurrentParticipantCount()>=resMatchupRequestDto.getMaxParticipants()){
            throw new CustomException("Matchup 현재 정원이 가득 찼습니다. 수정이 불가능합니다.");
        }

        // 1. 경기 시작 시간이 지났는지 체크

        if(resMatchupRequestDto.getMatchDatetime().isBefore(LocalDateTime.now())){
            throw new CustomException("Matchup 경기 시작 시간이 지나 수정할 수 없습니다.");
        }

        // 2. 수정가능한 상태인지 체크
        if(
                (resMatchupRequestDto.getMatchupStatus()==Status.PENDING && resMatchupRequestDto.getMatchupRequestSubmittedCount()==1 && resMatchupRequestDto.getMatchupCancelSubmittedCount()==0 && !resMatchupRequestDto.getIsDeleted()) ||
                (resMatchupRequestDto.getMatchupStatus()==Status.PENDING && resMatchupRequestDto.getMatchupRequestSubmittedCount()==2 && resMatchupRequestDto.getMatchupCancelSubmittedCount()==0 && !resMatchupRequestDto.getIsDeleted())
        ){
            return resMatchupRequestDto;
        }else{
            throw new CustomException("Matchup 참가 요청이 승인 되어 수정이 불가능합니다. 이력을 참고해주세요.");
        }

    }


    // 수정
    @Transactional
    public void updateMatchupRequest(ReqMatchupRequestEditDto reqMatchupRequestEditDto, Long requestId){
        MatchupRequest findMatchupRequest = matchupRequestRepository.findById(requestId).orElseThrow(()->new IllegalArgumentException("Matchup"+requestId+"번 요청은 없습니다."));

        // 1. 경기 시작 시간이 지났는지 체크

        if(reqMatchupRequestEditDto.getMatchDatetime().isBefore(LocalDateTime.now())){
            throw new CustomException("Matchup 경기 시작 시간이 지나 수정할 수 없습니다.");
        }

        // 수정 가능한 상태인지 체크
        if(
                (findMatchupRequest.getMatchupStatus()==Status.PENDING && findMatchupRequest.getMatchupRequestSubmittedCount()==1 && findMatchupRequest.getMatchupCancelSubmittedCount()==0 && !findMatchupRequest.getIsDeleted())||
                (findMatchupRequest.getMatchupStatus()==Status.PENDING && findMatchupRequest.getMatchupRequestSubmittedCount()==2 && findMatchupRequest.getMatchupCancelSubmittedCount()==0 && !findMatchupRequest.getIsDeleted())
        ){
                findMatchupRequest.update(reqMatchupRequestEditDto.getSelfIntro(), reqMatchupRequestEditDto.getParticipantCount());
        }
        // 재요청인 경우: 상태 업데이트 해야됨
        else if(findMatchupRequest.getMatchupStatus()==Status.PENDING && findMatchupRequest.getMatchupRequestSubmittedCount()==1 && findMatchupRequest.getMatchupCancelSubmittedCount()==0 && findMatchupRequest.getIsDeleted()){

                findMatchupRequest.updateRequestMangementInfo(Status.PENDING, 2, 0, false);
                findMatchupRequest.update(reqMatchupRequestEditDto.getSelfIntro(), reqMatchupRequestEditDto.getParticipantCount());

        }else if(findMatchupRequest.getMatchupStatus()==Status.DENIED && findMatchupRequest.getMatchupRequestSubmittedCount()==1 && findMatchupRequest.getMatchupCancelSubmittedCount()==0 && !findMatchupRequest.getIsDeleted()){

                findMatchupRequest.updateRequestMangementInfo(Status.PENDING, 2, 0, false);
                findMatchupRequest.update(reqMatchupRequestEditDto.getSelfIntro(), reqMatchupRequestEditDto.getParticipantCount());

        }else if(findMatchupRequest.getMatchupStatus()==Status.DENIED && findMatchupRequest.getMatchupRequestSubmittedCount()==1 && findMatchupRequest.getMatchupCancelSubmittedCount()==0 && findMatchupRequest.getIsDeleted()){

                findMatchupRequest.updateRequestMangementInfo(Status.PENDING, 2, 0, false);
                findMatchupRequest.update(reqMatchupRequestEditDto.getSelfIntro(), reqMatchupRequestEditDto.getParticipantCount());
        }
        else{
            throw new CustomException("Matchup 수정하기 또는 재요청이 불가능합니다. 요청 이력을 참고해주세요.");
        }
    }



    // 참가 취소
    @Transactional
    public void cancelMatchupRequestBeforeApproval(Long boardId, Long requestId, CustomUser user) {
        MatchupRequest findMatchupRequest = matchupRequestRepository.findMatchupRequestWithMatchupBoardByRequestIdAndBoardIDAndApplicantId(requestId, boardId, user.getMember().getId()).orElseThrow(()->new CustomException("Matchup 요청을 찾을 수 없습니다."));

        //날짜
        if(findMatchupRequest.getMatchupBoard().getMatchDatetime().isBefore(LocalDateTime.now())){
            throw new CustomException("Matchup 경기 시작 시간이 지나 참가 요청할 수 없습니다.");
        }

        //상태
        if(findMatchupRequest.getMatchupStatus()==Status.PENDING && findMatchupRequest.getMatchupRequestSubmittedCount()==1 && findMatchupRequest.getMatchupCancelSubmittedCount() ==0 && !findMatchupRequest.getIsDeleted()){

            findMatchupRequest.updateRequestMangementInfo(Status.PENDING, findMatchupRequest.getMatchupRequestSubmittedCount(), findMatchupRequest.getMatchupCancelSubmittedCount(), !findMatchupRequest.getIsDeleted());

        } else if(findMatchupRequest.getMatchupStatus()==Status.PENDING && findMatchupRequest.getMatchupRequestSubmittedCount()==2 && findMatchupRequest.getMatchupCancelSubmittedCount() ==0 && !findMatchupRequest.getIsDeleted()){

            findMatchupRequest.updateRequestMangementInfo(Status.PENDING, findMatchupRequest.getMatchupRequestSubmittedCount(), findMatchupRequest.getMatchupCancelSubmittedCount(), !findMatchupRequest.getIsDeleted());

        } else{
            throw new CustomException("Matchup 참가 요청 취소가 불가능합니다. 이력을 참고해주세요.");
        }
    }

    // 재요청
    @Transactional
    public ResMatchupRequestDto retryMatchupRequest(Long boardId, Long requestId, CustomUser user) {

        MatchupRequest findMatchupRequest = matchupRequestRepository.findMatchupRequestWithMatchupBoardByRequestIdAndBoardIDAndApplicantId(requestId, boardId, user.getMember().getId()).orElseThrow(()->new CustomException("Matchup 요청을 찾을 수 없습니다."));

        //날짜
        if(findMatchupRequest.getMatchupBoard().getMatchDatetime().isBefore(LocalDateTime.now())){
            throw new CustomException("Matchup 경기 시작 시간이 지나 재요청할 수 없습니다.");
        }

        if(findMatchupRequest.getMatchupStatus()==Status.PENDING && findMatchupRequest.getMatchupRequestSubmittedCount()==1 && findMatchupRequest.getMatchupCancelSubmittedCount()==0 && findMatchupRequest.getIsDeleted()){

            findMatchupRequest.updateRequestMangementInfo(Status.PENDING, findMatchupRequest.getMatchupRequestSubmittedCount()+1, findMatchupRequest.getMatchupCancelSubmittedCount(), !findMatchupRequest.getIsDeleted());

        }else if(findMatchupRequest.getMatchupStatus()==Status.DENIED && findMatchupRequest.getMatchupRequestSubmittedCount()==1 && findMatchupRequest.getMatchupCancelSubmittedCount()==0 && !findMatchupRequest.getIsDeleted()){

            findMatchupRequest.updateRequestMangementInfo(Status.PENDING, findMatchupRequest.getMatchupRequestSubmittedCount()+1, findMatchupRequest.getMatchupCancelSubmittedCount(), findMatchupRequest.getIsDeleted());

        }else{
            throw new CustomException("Matchup 현재 재요청이 불가능합니다. 요청 이력을 확인해주세요.");
        }

        return matchupRequestRepository.findResMatchupRequestDtoByRequestId(requestId).orElseThrow(()->new IllegalArgumentException("Matchup"+requestId+"번 요청은 없습니다."));
    }



    // 승인 취소 요청
    @Transactional
    public void matchupRequestCancelAfterApproval(Long boardId, Long requestId, CustomUser user) {
        MatchupRequest findMatchupRequest = matchupRequestRepository.findMatchupRequestWithMatchupBoardByRequestIdAndBoardIDAndApplicantId(requestId, boardId, user.getMember().getId()).orElseThrow(()->new CustomException("Matchup 요청을 찾을 수 없습니다."));

        //날짜
        if(findMatchupRequest.getMatchupBoard().getMatchDatetime().isBefore(LocalDateTime.now())){
            throw new CustomException("Matchup 경기 시작 시간이 지나 승인 취소 요청할 수 없습니다.");
        }

        //상태
        if(findMatchupRequest.getMatchupStatus()==Status.APPROVED && findMatchupRequest.getMatchupRequestSubmittedCount()==2 && findMatchupRequest.getMatchupCancelSubmittedCount()==0 && !findMatchupRequest.getIsDeleted()){

            findMatchupRequest.updateRequestMangementInfo(Status.CANCELREQUESTED, findMatchupRequest.getMatchupRequestSubmittedCount(), findMatchupRequest.getMatchupCancelSubmittedCount()+1, findMatchupRequest.getIsDeleted());

        }else if(findMatchupRequest.getMatchupStatus()==Status.APPROVED && findMatchupRequest.getMatchupRequestSubmittedCount()==1 && findMatchupRequest.getMatchupCancelSubmittedCount()==0 && !findMatchupRequest.getIsDeleted()){

            findMatchupRequest.updateRequestMangementInfo(Status.CANCELREQUESTED, findMatchupRequest.getMatchupRequestSubmittedCount(), findMatchupRequest.getMatchupCancelSubmittedCount()+1, findMatchupRequest.getIsDeleted());

        }else{
            throw new CustomException("Matchup 승인 취소 요청이 불가능합니다. 이력을 참고해주세요.");
        }

    }

    // 참가 요청 승인, 승인 취소 요청 승인
    @Transactional
    public void approveMatchupRequest(Long boardId, Long requestId, CustomUser user) {
        MatchupRequest findMatchupRequest = matchupRequestRepository.findMatchupRequestWithMatchupBoardByRequestIdAndBoardIDAndWriterId(requestId, boardId, user.getMember().getId()).orElseThrow(()->new CustomException("Matchup 요청을 찾을 수 없습니다."));

        //날짜
        if(findMatchupRequest.getMatchupBoard().getMatchDatetime().isBefore(LocalDateTime.now())){
            throw new CustomException("Matchup 경기 시작 시간이 지나 승인할 수 없습니다.");
        }

        // 참가 요청 승인: 상태 확인 → 인원 수 체크 → 업데이트

        // 재요청에 대한 승인
        if(findMatchupRequest.getMatchupStatus()==Status.PENDING && findMatchupRequest.getMatchupRequestSubmittedCount()==2 && findMatchupRequest.getMatchupCancelSubmittedCount()==0 && !findMatchupRequest.getIsDeleted()){


            if(findMatchupRequest.getMatchupBoard().getCurrentParticipantCount()+findMatchupRequest.getParticipantCount()>findMatchupRequest.getMatchupBoard().getMaxParticipants()){

                throw new CustomException("Matchup 모집 인원이 초과됩니다.");
            }else{

                findMatchupRequest.updateRequestMangementInfo(Status.APPROVED, findMatchupRequest.getMatchupRequestSubmittedCount(), findMatchupRequest.getMatchupCancelSubmittedCount(), findMatchupRequest.getIsDeleted());

                findMatchupRequest.getMatchupBoard().increaseCurrentParticipantCount(findMatchupRequest.getParticipantCount());

                chatService.addParticipantToGroupChat(findMatchupRequest.getMatchupBoard().getChatRoom(), findMatchupRequest.getMember());


            }

        //최초 요청에 대한 승인
        }else if(findMatchupRequest.getMatchupStatus()==Status.PENDING && findMatchupRequest.getMatchupRequestSubmittedCount()==1 && findMatchupRequest.getMatchupCancelSubmittedCount()==0 && !findMatchupRequest.getIsDeleted()){

            if(findMatchupRequest.getMatchupBoard().getCurrentParticipantCount()+findMatchupRequest.getParticipantCount()>findMatchupRequest.getMatchupBoard().getMaxParticipants()){
                throw new CustomException("Matchup 모집 인원이 초과됩니다.");

            }else{
                findMatchupRequest.updateRequestMangementInfo(Status.APPROVED, findMatchupRequest.getMatchupRequestSubmittedCount(), findMatchupRequest.getMatchupCancelSubmittedCount(), findMatchupRequest.getIsDeleted());

                findMatchupRequest.getMatchupBoard().increaseCurrentParticipantCount(findMatchupRequest.getParticipantCount());

                chatService.addParticipantToGroupChat(findMatchupRequest.getMatchupBoard().getChatRoom(), findMatchupRequest.getMember());

            }
        }

        // 승인 취소 요청에 대한 승인: 승인 상태 확인 → 인원 수 체크 → 업데이트

        // 재요청 → 승인 → 승인 취소 요청 → 승인
        else if(findMatchupRequest.getMatchupStatus()==Status.CANCELREQUESTED && findMatchupRequest.getMatchupRequestSubmittedCount()==2 && findMatchupRequest.getMatchupCancelSubmittedCount()==1 && !findMatchupRequest.getIsDeleted()){

                findMatchupRequest.updateRequestMangementInfo(Status.CANCELREQUESTED, findMatchupRequest.getMatchupRequestSubmittedCount(), findMatchupRequest.getMatchupCancelSubmittedCount(), !findMatchupRequest.getIsDeleted());

                findMatchupRequest.getMatchupBoard().decreaseCurrentParticipantCount(findMatchupRequest.getParticipantCount());

                chatService.removeParticipantToGroupChat(findMatchupRequest.getMatchupBoard().getChatRoom(), findMatchupRequest.getMember());

        // 최초 요청 → 승인 → 승인 취소 요청 → 승인
        } else if(findMatchupRequest.getMatchupStatus()==Status.CANCELREQUESTED && findMatchupRequest.getMatchupRequestSubmittedCount()==1 && findMatchupRequest.getMatchupCancelSubmittedCount()==1 && !findMatchupRequest.getIsDeleted()){

            findMatchupRequest.updateRequestMangementInfo(Status.CANCELREQUESTED, findMatchupRequest.getMatchupRequestSubmittedCount(), findMatchupRequest.getMatchupCancelSubmittedCount(), !findMatchupRequest.getIsDeleted());

            findMatchupRequest.getMatchupBoard().decreaseCurrentParticipantCount(findMatchupRequest.getParticipantCount());

            chatService.removeParticipantToGroupChat(findMatchupRequest.getMatchupBoard().getChatRoom(), findMatchupRequest.getMember());

        } else{
            throw new CustomException("Matchup 현재 승인이 불가능합니다. 요청 목록을 참고해주세요.");
        }
    }

    // 참가 요청 반려, 승인 취소 요청 반려
    @Transactional
    public void denyMatchupRequest(Long boardId, Long requestId, CustomUser user) {
        MatchupRequest findMatchupRequest = matchupRequestRepository.findMatchupRequestWithMatchupBoardByRequestIdAndBoardIDAndWriterId(requestId, boardId, user.getMember().getId()).orElseThrow(()->new CustomException("Matchup 요청을 찾을 수 없습니다."));

        //날짜
        if(findMatchupRequest.getMatchupBoard().getMatchDatetime().isBefore(LocalDateTime.now())){
            throw new CustomException("Matchup 경기 시작 시간이 지나 반려할 수 없습니다.");
        }

        //인원수
        //상태

        // 참가 요청 반려: 상태 확인 → 업데이트

        // 재요청에 대한 반려
        if(findMatchupRequest.getMatchupStatus()==Status.PENDING && findMatchupRequest.getMatchupRequestSubmittedCount()==2 && findMatchupRequest.getMatchupCancelSubmittedCount()==0 && !findMatchupRequest.getIsDeleted()){

            findMatchupRequest.updateRequestMangementInfo(Status.DENIED, findMatchupRequest.getMatchupRequestSubmittedCount(), findMatchupRequest.getMatchupCancelSubmittedCount(), findMatchupRequest.getIsDeleted());

        // 최초 요청에 대한 반려
        }else if(findMatchupRequest.getMatchupStatus()==Status.PENDING && findMatchupRequest.getMatchupRequestSubmittedCount()==1 && findMatchupRequest.getMatchupCancelSubmittedCount()==0 && !findMatchupRequest.getIsDeleted()){

            findMatchupRequest.updateRequestMangementInfo(Status.DENIED, findMatchupRequest.getMatchupRequestSubmittedCount(), findMatchupRequest.getMatchupCancelSubmittedCount(), findMatchupRequest.getIsDeleted());

        }

        // 승인 취소 요청에 대한 반려: 승인 상태 확인 →  업데이트

        // 재요청 → 승인 → 승인 취소 요청 → 반려
        else if(findMatchupRequest.getMatchupStatus()==Status.CANCELREQUESTED && findMatchupRequest.getMatchupRequestSubmittedCount()==2 && findMatchupRequest.getMatchupCancelSubmittedCount()==1 && !findMatchupRequest.getIsDeleted()){

            findMatchupRequest.updateRequestMangementInfo(Status.APPROVED, findMatchupRequest.getMatchupRequestSubmittedCount(), findMatchupRequest.getMatchupCancelSubmittedCount(), findMatchupRequest.getIsDeleted());


        // 최초 요청 → 승인 → 승인 취소 요청 → 반려
        } else if(findMatchupRequest.getMatchupStatus()==Status.CANCELREQUESTED && findMatchupRequest.getMatchupRequestSubmittedCount()==1 && findMatchupRequest.getMatchupCancelSubmittedCount()==1 && !findMatchupRequest.getIsDeleted()){

            findMatchupRequest.updateRequestMangementInfo(Status.APPROVED, findMatchupRequest.getMatchupRequestSubmittedCount(), findMatchupRequest.getMatchupCancelSubmittedCount(), findMatchupRequest.getIsDeleted());

        } else{
            throw new CustomException("Matchup 현재 반려가 불가능합니다. 요청 목록을 참고해주세요.");

        }
    }
    // 삭제
}
