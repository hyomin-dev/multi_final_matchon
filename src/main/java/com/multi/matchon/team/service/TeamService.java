package com.multi.matchon.team.service;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.domain.SportsTypeName;
import com.multi.matchon.common.repository.AttachmentRepository;
import com.multi.matchon.common.repository.SportsTypeRepository;

import com.multi.matchon.matchup.domain.MatchupBoard;
import com.multi.matchon.team.domain.Team;
import com.multi.matchon.team.dto.req.ReqTeamDto;
import com.multi.matchon.team.repository.TeamNameRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeamService {
    //@Value("${spring.cloud.aws.s3.base-url}")
    private String S3_URL;
    private String FILE_DIR = "attachments/";
    private String FILE_URL;

    @PostConstruct
    public void init() {
        this.FILE_URL = S3_URL;
    }


    private final TeamNameRepository teamBoardRepository;

    private final SportsTypeRepository sportsTypeRepository;
    private final AttachmentRepository attachmentRepository;

//    private final AwsS3Utils awsS3Utils;


    public List<Team> findAll() {
        List<Team> teamBoards = teamBoardRepository.findAll();


        return teamBoards;
    }
}

//    public void teamRegister(ReqTeamDto reqTeamDto, CustomUser user) {
//        MatchupBoard newMatchupBoard = MatchupBoard.builder()
//                .member(user.getMember())
//                .sportsType(sportsTypeRepository.findBySportsTypeName(SportsTypeName.valueOf(reqMatchupBoardDto.getSportsType())).orElseThrow(()-> new IllegalArgumentException(reqMatchupBoardDto.getSportsType()+"는 지원하지 않는 종목입니다.")))
//                .reservationAttachmentEnabled(true)
//                .teamIntro(reqMatchupBoardDto.getTeamIntro())
//                .sportsFacilityName(reqMatchupBoardDto.getSportsFacilityName())
//                .sportsFacilityAddress(reqMatchupBoardDto.getSportsFacilityAddress())
//                .matchDatetime(reqMatchupBoardDto.getMatchDate())
//                .matchDuration(LocalTime.of(reqMatchupBoardDto.getMatchDuration()/60,reqMatchupBoardDto.getMatchDuration()%60))
//                .currentParticipantCount(reqMatchupBoardDto.getCurrentParticipants())
//                .maxParticipants(reqMatchupBoardDto.getMaxParticipants())
//                .minMannerTemperature(reqMatchupBoardDto.getMinMannerTemperature())
//                .matchDescription(reqMatchupBoardDto.getMatchIntro())
//                .build();
//        MatchupBoard matchupBoard = matchupBoardRepository.save(newMatchupBoard);
//        insertFile(reqMatchupBoardDto, matchupBoard);
//    }
