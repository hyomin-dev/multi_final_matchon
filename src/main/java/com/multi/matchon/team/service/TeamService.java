package com.multi.matchon.team.service;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.domain.PositionName;
import com.multi.matchon.common.domain.Positions;
import com.multi.matchon.common.domain.SportsTypeName;
import com.multi.matchon.common.repository.AttachmentRepository;
import com.multi.matchon.common.repository.PositionsRepository;
import com.multi.matchon.common.repository.SportsTypeRepository;

import com.multi.matchon.matchup.domain.MatchupBoard;
import com.multi.matchon.team.domain.RecruitingPosition;
import com.multi.matchon.team.domain.RegionType;
import com.multi.matchon.team.domain.Team;
import com.multi.matchon.team.dto.req.ReqTeamDto;
import com.multi.matchon.team.repository.RecruitingPositionRepository;
import com.multi.matchon.team.repository.TeamNameRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.data.redis.connection.RedisListCommands;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeamService {
    private final ClientHttpRequestFactorySettings clientHttpRequestFactorySettings;
    //@Value("${spring.cloud.aws.s3.base-url}")
    private String S3_URL;
    private String FILE_DIR = "attachments/";
    private String FILE_URL;

    @PostConstruct
    public void init() {
        this.FILE_URL = S3_URL;
    }


    private final TeamNameRepository teamBoardRepository;

    private final RecruitingPositionRepository recruitingPositionRepository;

    private final PositionsRepository positionsRepository;
    private final AttachmentRepository attachmentRepository;

//    private final AwsS3Utils awsS3Utils;


    public List<Team> findAll() {
        List<Team> teamBoards = teamBoardRepository.findAll();


        return teamBoards;
    }


    public void teamRegister(ReqTeamDto reqTeamDto, CustomUser user) {
        Team newTeam = Team.builder()
                .teamName(reqTeamDto.getTeamName())
                .teamRegion(RegionType.valueOf(reqTeamDto.getTeamRegion()))
                .teamRatingAverage(reqTeamDto.getTeamRatingAverage())
                .recruitmentStatus(false)
                .teamIntroduction(reqTeamDto.getTeamIntroduction())
                .teamAttachmentEnabled(true)
                .build();
        Team savedTeam = teamBoardRepository.save(newTeam);

        for (String posName : reqTeamDto.getRecruitingPositions()) {
            PositionName enumVal = PositionName.valueOf(posName.trim());
            Positions posEntity = positionsRepository.findByPositionName(enumVal);

            RecruitingPosition rp = RecruitingPosition.builder()
                    .team(savedTeam)
                    .positions(posEntity)
                    .build();

            recruitingPositionRepository.save(rp);
        }

        insertFile(reqTeamDto, savedTeam);
    }

    private void insertFile(ReqTeamDto reqTeamDto, Team team) {
    }
}
