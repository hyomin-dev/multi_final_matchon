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
import com.multi.matchon.matchup.dto.req.ReqMatchupBoardDto;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardListDto;
import com.multi.matchon.matchup.repository.MatchupBoardRepository;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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

    private final SportsTypeRepository sportsTypeRepository;
    private final AttachmentRepository attachmentRepository;

    private final AwsS3Utils awsS3Utils;



    public List<MatchupBoard> findAll() {
        List<MatchupBoard> matchupBoards = matchupBoardRepository.findAll();
        List<MatchupBoard> matchupBoards1 = matchupBoardRepository.findAllWithMember();
        List<MatchupBoard> matchupBoards2 = matchupBoardRepository.findAllWithMemberAndWithSportsType();

        return matchupBoards;
    }

    public void boardRegister(ReqMatchupBoardDto reqMatchupBoardDto, CustomUser user) {

        MatchupBoard newMatchupBoard = MatchupBoard.builder()
                .member(user.getMember())
                .sportsType(sportsTypeRepository.findBySportsTypeName(SportsTypeName.valueOf(reqMatchupBoardDto.getSportsType())).orElseThrow(()-> new IllegalArgumentException(reqMatchupBoardDto.getSportsType()+"는 지원하지 않는 종목입니다.")))
                .reservationAttachmentEnabled(true)
                .teamIntro(reqMatchupBoardDto.getTeamIntro())
                .sportsFacilityName(reqMatchupBoardDto.getSportsFacilityName())
                .sportsFacilityAddress(reqMatchupBoardDto.getSportsFacilityAddress())
                .matchDatetime(reqMatchupBoardDto.getMatchDate())
                .matchDuration(LocalTime.of(reqMatchupBoardDto.getMatchDuration()/60,reqMatchupBoardDto.getMatchDuration()%60))
                .currentParticipantCount(reqMatchupBoardDto.getCurrentParticipants())
                .maxParticipants(reqMatchupBoardDto.getMaxParticipants())
                .minMannerTemperature(reqMatchupBoardDto.getMinMannerTemperature())
                .matchDescription(reqMatchupBoardDto.getMatchIntro())
                .build();
        MatchupBoard matchupBoard = matchupBoardRepository.save(newMatchupBoard);
        insertFile(reqMatchupBoardDto, matchupBoard);

    }
    public void insertFile(ReqMatchupBoardDto reqMatchupBoardDto, MatchupBoard matchupBoard){
        String fileName = UUID.randomUUID().toString().replace("-","");
        awsS3Utils.saveFile(FILE_DIR, fileName, reqMatchupBoardDto.getReservationFile());
        Attachment attachment = Attachment.builder()
                .boardType(BoardType.MATCHUP_BOARD)
                .boardNumber(matchupBoard.getId())
                .fileOrder(0)
                .originalName(reqMatchupBoardDto.getReservationFile().getOriginalFilename())
                .savedName(fileName+reqMatchupBoardDto.getReservationFile().getOriginalFilename().substring(reqMatchupBoardDto.getReservationFile().getOriginalFilename().indexOf(".")))
                .savePath(FILE_DIR)
                .build();
        attachmentRepository.save(attachment);


    }

    public void findBoardListTest() {
        Pageable pageRequest1 = PageRequest.of(0,4);
        Pageable pageRequest2 = PageRequest.of(1,4);
        Pageable pageRequest3 = PageRequest.of(2,4);
        Pageable pageRequest4 = PageRequest.of(3,4);
        Pageable pageRequest5 = PageRequest.of(4,4);

        List<ResMatchupBoardListDto> resMatchupBoardDtos = matchupBoardRepository.findBoardListTest();
        Page<ResMatchupBoardListDto> resMatchupBoardDtos1 = matchupBoardRepository.findBoardListTest2(pageRequest1);
        Page<ResMatchupBoardListDto> resMatchupBoardDtos2 = matchupBoardRepository.findBoardListTest2(pageRequest2);
        Page<ResMatchupBoardListDto> resMatchupBoardDtos3 = matchupBoardRepository.findBoardListTest2(pageRequest3);
        Page<ResMatchupBoardListDto> resMatchupBoardDtos4 = matchupBoardRepository.findBoardListTest2(pageRequest4);
        Page<ResMatchupBoardListDto> resMatchupBoardDtos5 = matchupBoardRepository.findBoardListTest2(pageRequest5);




        System.out.println("resMatchupBoardDtos = " + resMatchupBoardDtos);
        System.out.println("resMatchupBoardDtos1 = " + resMatchupBoardDtos1);
        System.out.println("resMatchupBoardDtos2 = " + resMatchupBoardDtos2);
        System.out.println("resMatchupBoardDtos3 = " + resMatchupBoardDtos3);
        System.out.println("resMatchupBoardDtos4 = " + resMatchupBoardDtos4);
        System.out.println("resMatchupBoardDtos5 = " + resMatchupBoardDtos5);

        PageResponseDto<ResMatchupBoardListDto> pageResponseDtos = PageResponseDto.<ResMatchupBoardListDto>builder()
                .items(resMatchupBoardDtos1.getContent())
                .pageInfo(PageResponseDto.PageInfoDto.builder()
                        .page(resMatchupBoardDtos1.getNumber())
                        .size(resMatchupBoardDtos1.getNumberOfElements())
                        .totalElements(resMatchupBoardDtos1.getTotalElements())
                        .totalPages(resMatchupBoardDtos1.getTotalPages())
                        .isFirst(resMatchupBoardDtos1.isFirst())
                        .isLast(resMatchupBoardDtos1.isLast())
                        .build())
                .build();

        System.out.println();
    }

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
}
