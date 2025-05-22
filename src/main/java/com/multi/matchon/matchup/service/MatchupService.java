package com.multi.matchon.matchup.service;


import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.domain.Attachment;
import com.multi.matchon.common.domain.BoardType;
import com.multi.matchon.common.domain.SportsTypeName;
import com.multi.matchon.common.dto.res.PageResponseDto;
import com.multi.matchon.common.repository.AttachmentRepository;
import com.multi.matchon.common.repository.SportsTypeRepository;

import com.multi.matchon.common.util.AwsS3Utils;
import com.multi.matchon.matchup.domain.MatchupBoard;
import com.multi.matchon.matchup.domain.MatchupRequest;
import com.multi.matchon.matchup.dto.req.ReqMatchupBoardDto;
import com.multi.matchon.matchup.dto.req.ReqMatchupRequestDto;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardDto;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardListDto;
import com.multi.matchon.matchup.dto.res.ResMatchupRequestDto;
import com.multi.matchon.matchup.dto.res.ResMatchupRequestListDto;
import com.multi.matchon.matchup.repository.MatchupBoardRepository;
import com.multi.matchon.matchup.repository.MatchupRequestRepository;
import com.multi.matchon.member.domain.Member;
import com.sun.jdi.request.DuplicateRequestException;
import io.awspring.cloud.s3.S3Resource;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;



import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MatchupService{

    @Value("${spring.cloud.aws.s3.base-url}")
    private String S3_URL;
    private String FILE_DIR = "attachments/";
    private String FILE_URL;
    @PostConstruct
    public void init(){
        this.FILE_URL = S3_URL;
    }


    private final MatchupBoardRepository matchupBoardRepository;
    private final MatchupRequestRepository matchupRequestRepository;

    private final SportsTypeRepository sportsTypeRepository;
    private final AttachmentRepository attachmentRepository;

    private final AwsS3Utils awsS3Utils;



//    public List<MatchupBoard> findAll() {
//        List<MatchupBoard> matchupBoards = matchupBoardRepository.findAll();
//        List<MatchupBoard> matchupBoards1 = matchupBoardRepository.findAllWithMember();
//        List<MatchupBoard> matchupBoards2 = matchupBoardRepository.findAllWithMemberAndWithSportsType();
//
//        return matchupBoards;
//    }

    public void boardRegister(ReqMatchupBoardDto reqMatchupBoardDto, CustomUser user) {

        MatchupBoard newMatchupBoard = MatchupBoard.builder()
                .member(user.getMember())
                .sportsType(sportsTypeRepository.findBySportsTypeName(SportsTypeName.valueOf(reqMatchupBoardDto.getSportsTypeName())).orElseThrow(()-> new IllegalArgumentException(reqMatchupBoardDto.getSportsTypeName()+"는 지원하지 않는 종목입니다.")))
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
        insertFile(reqMatchupBoardDto.getReservationFile(), matchupBoard);

    }
    public void insertFile(MultipartFile multipartFile, MatchupBoard matchupBoard){
        String fileName = UUID.randomUUID().toString().replace("-","");
        awsS3Utils.saveFile(FILE_DIR, fileName, multipartFile);
        Attachment attachment = Attachment.builder()
                .boardType(BoardType.MATCHUP_BOARD)
                .boardNumber(matchupBoard.getId())
                .fileOrder(0)
                .originalName(multipartFile.getOriginalFilename())
                .savedName(fileName+multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().indexOf(".")))
                .savePath(FILE_DIR)
                .build();
        attachmentRepository.save(attachment);
    }

    public void updateFile(MultipartFile multipartFile, MatchupBoard findMatchupBoard){
        String fileName = UUID.randomUUID().toString().replace("-","");

        List<Attachment> findAttachments = attachmentRepository.findAllByBoardTypeAndBoardNumber(BoardType.MATCHUP_BOARD, findMatchupBoard.getId());

        if(findAttachments.isEmpty())
            throw new IllegalArgumentException(BoardType.MATCHUP_BOARD+"타입, "+findMatchupBoard.getId()+"번에는 첨부파일이 없습니다.");

        String savedName = findAttachments.get(0).getSavedName();

        awsS3Utils.deleteFile(FILE_DIR, savedName.substring(0,savedName.indexOf(".")));

        findAttachments.get(0).update(multipartFile.getOriginalFilename(), fileName+multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().indexOf(".")), FILE_DIR);

        awsS3Utils.saveFile(FILE_DIR, fileName, multipartFile);

    }


//    public void findBoardListTest() {
//        Pageable pageRequest1 = PageRequest.of(0,4);
//        Pageable pageRequest2 = PageRequest.of(1,4);
//        Pageable pageRequest3 = PageRequest.of(2,4);
//        Pageable pageRequest4 = PageRequest.of(3,4);
//        Pageable pageRequest5 = PageRequest.of(4,4);
//
//        List<ResMatchupBoardListDto> resMatchupBoardDtos = matchupBoardRepository.findBoardListTest();
//        Page<ResMatchupBoardListDto> resMatchupBoardDtos1 = matchupBoardRepository.findBoardListTest2(pageRequest1);
//        Page<ResMatchupBoardListDto> resMatchupBoardDtos2 = matchupBoardRepository.findBoardListTest2(pageRequest2);
//        Page<ResMatchupBoardListDto> resMatchupBoardDtos3 = matchupBoardRepository.findBoardListTest2(pageRequest3);
//        Page<ResMatchupBoardListDto> resMatchupBoardDtos4 = matchupBoardRepository.findBoardListTest2(pageRequest4);
//        Page<ResMatchupBoardListDto> resMatchupBoardDtos5 = matchupBoardRepository.findBoardListTest2(pageRequest5);
//
//
//
//
//        System.out.println("resMatchupBoardDtos = " + resMatchupBoardDtos);
//        System.out.println("resMatchupBoardDtos1 = " + resMatchupBoardDtos1);
//        System.out.println("resMatchupBoardDtos2 = " + resMatchupBoardDtos2);
//        System.out.println("resMatchupBoardDtos3 = " + resMatchupBoardDtos3);
//        System.out.println("resMatchupBoardDtos4 = " + resMatchupBoardDtos4);
//        System.out.println("resMatchupBoardDtos5 = " + resMatchupBoardDtos5);
//
//        PageResponseDto<ResMatchupBoardListDto> pageResponseDtos = PageResponseDto.<ResMatchupBoardListDto>builder()
//                .items(resMatchupBoardDtos1.getContent())
//                .pageInfo(PageResponseDto.PageInfoDto.builder()
//                        .page(resMatchupBoardDtos1.getNumber())
//                        .size(resMatchupBoardDtos1.getNumberOfElements())
//                        .totalElements(resMatchupBoardDtos1.getTotalElements())
//                        .totalPages(resMatchupBoardDtos1.getTotalPages())
//                        .isFirst(resMatchupBoardDtos1.isFirst())
//                        .isLast(resMatchupBoardDtos1.isLast())
//                        .build())
//                .build();
//
//        System.out.println();
//    }

    public PageResponseDto<ResMatchupBoardListDto> findAllWithPaging(PageRequest pageRequest, String sportsType, String region, String date  ) {
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

        Page<ResMatchupBoardListDto> page = matchupBoardRepository.findBoardListWithPaging(pageRequest, sportsTypeName, region, matchDate);
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

    public PageResponseDto<ResMatchupBoardListDto> findMyAllWithPaging(PageRequest pageRequest, CustomUser user) {

        Page<ResMatchupBoardListDto> page = matchupBoardRepository.findByMemberEmailBoardListWithPaging(pageRequest, user.getMember().getMemberEmail());
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

    public ResMatchupBoardDto findBoardByBoardId(Long boardId) {

        MatchupBoard matchupBoard = matchupBoardRepository.findByIdWithMemberWithTeamWithSportsType(boardId).orElseThrow(()->new IllegalArgumentException(boardId +"번 게시글이 존재하지 않습니다."));

        List<Attachment> attachments = attachmentRepository.findAllByBoardTypeAndBoardNumber(BoardType.MATCHUP_BOARD, boardId);

        if(attachments.isEmpty()&&matchupBoard.getReservationAttachmentEnabled())
            throw new IllegalArgumentException(boardId +"번 게시글의 첨부파일이 존재해야하는데 없습니다.");

       return ResMatchupBoardDto.builder()
                .boardId(matchupBoard.getId())
                .writer(matchupBoard.getMember().getMemberEmail())
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
                .myTemperature(matchupBoard.getMember().getMyTemperature())
                .matchDescription(matchupBoard.getMatchDescription())
                .originalName(attachments.get(0).getOriginalName())
                .savedName(attachments.get(0).getSavedName())
                .savedPath(attachments.get(0).getSavePath())
                .build();

    }

    public void boardEdit(ResMatchupBoardDto resMatchupBoardDto) {
        MatchupBoard findMatchupBoard = matchupBoardRepository.findByIdAndIsDeleted(resMatchupBoardDto.getBoardId()).orElseThrow(()->new IllegalArgumentException(resMatchupBoardDto.getBoardId()+"번 게시글이 없습니다."));

        findMatchupBoard.update(
                sportsTypeRepository.findBySportsTypeName(resMatchupBoardDto.getSportsTypeName()).orElseThrow(()-> new IllegalArgumentException(resMatchupBoardDto.getSportsTypeName()+"는 지원하지 않는 종목입니다.")),
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

            updateFile(resMatchupBoardDto.getReservationFile(), findMatchupBoard);

        }
    }

    public S3Resource getAttachmentFile(String savedName) throws IOException {
        S3Resource resource = awsS3Utils.downloadFile(FILE_DIR, savedName);
        return resource;
    }

    public String getAttachmentURL(String savedName) throws IOException {
        String presignedUrl = awsS3Utils.createPresignedGetUrl(FILE_DIR, savedName);

        return presignedUrl;
    }


    public void boardDelete(Long boardId) {
        MatchupBoard findMatchupBoard = matchupBoardRepository.findByIdAndIsDeleted(boardId).orElseThrow(()->new IllegalArgumentException(boardId+"번 게시글이 없습니다."));

        findMatchupBoard.delete(true);

        matchupBoardRepository.save(findMatchupBoard);

        List<Attachment> findAttachments = attachmentRepository.findAllByBoardTypeAndBoardNumber(BoardType.MATCHUP_BOARD, boardId);
        if(findAttachments.isEmpty())
            throw new IllegalArgumentException(BoardType.MATCHUP_BOARD+"타입, "+findMatchupBoard.getId()+"번에는 첨부파일이 없습니다.");
        findAttachments.get(0).delete(true);

        attachmentRepository.save(findAttachments.get(0));

    }

    public ReqMatchupRequestDto findReqMatchupRequestDtoByBoardId(Long boardId) {

        ReqMatchupRequestDto reqMatchupRequestDto = matchupBoardRepository.findReqMatchupRequestDtoByBoardId(boardId).orElseThrow(()->new IllegalArgumentException(boardId+"번 게시글이 업습니다."));

        return reqMatchupRequestDto;
    }

    public void requestRegister(ReqMatchupRequestDto reqMatchupRequestDto, Member member) {

        Boolean isDuplicate = matchupBoardRepository.isAlreadyRequestedByBoardIdAndMemberId(reqMatchupRequestDto.getBoardId(), member.getId());

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

    public PageResponseDto<ResMatchupRequestListDto> findMyRequestAllWithPaging(PageRequest pageRequest, CustomUser user, String sportsType, String date) {

        SportsTypeName sportsTypeName;
        if(sportsType.isBlank())
            sportsTypeName = null;
        else
            sportsTypeName = SportsTypeName.valueOf(sportsType);

        LocalDate matchDate = null;
        if(!date.isBlank())
            matchDate = LocalDate.parse(date);


        Page<ResMatchupRequestListDto> page = matchupRequestRepository.findMyRequestAllWithPaging(pageRequest,user.getMember().getId(), sportsTypeName, matchDate);

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

    public ResMatchupRequestDto findResMatchRequestDtoByRequestId(Long requestId) {


        return matchupRequestRepository.findResMatchRequestDtoByRequestId(requestId).orElseThrow(()->new IllegalArgumentException(requestId+"번 요청은 없습니다."));

    }
}















































