package com.multi.matchon.team.service;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.domain.*;
import com.multi.matchon.common.dto.res.PageResponseDto;
import com.multi.matchon.common.repository.AttachmentRepository;
import com.multi.matchon.common.repository.PositionsRepository;
import com.multi.matchon.common.repository.SportsTypeRepository;

import com.multi.matchon.common.util.AwsS3Utils;
import com.multi.matchon.matchup.domain.MatchupBoard;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardListDto;
import com.multi.matchon.team.domain.RecruitingPosition;
import com.multi.matchon.team.domain.RegionType;
import com.multi.matchon.team.domain.Team;
import com.multi.matchon.team.dto.req.ReqTeamDto;
import com.multi.matchon.team.dto.res.ResTeamDto;
import com.multi.matchon.team.repository.RecruitingPositionRepository;
import com.multi.matchon.team.repository.TeamNameRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.connection.RedisListCommands;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Value("${spring.cloud.aws.s3.base-url}")
    private String S3BaseUrl;

    @PostConstruct
    public void init() {
        this.FILE_URL = S3_URL;
    }


    private final TeamNameRepository teamBoardRepository;

    private final RecruitingPositionRepository recruitingPositionRepository;

    private final PositionsRepository positionsRepository;
    private final AttachmentRepository attachmentRepository;

    private final AwsS3Utils awsS3Utils;


    public List<Team> findAll() {
        List<Team> teamBoards = teamBoardRepository.findAll();


        return teamBoards;
    }


    public void teamRegister(ReqTeamDto reqTeamDto, CustomUser user) {
        Team newTeam = Team.builder()
                .teamName(reqTeamDto.getTeamName())
                .teamRegion(RegionType.valueOf(reqTeamDto.getTeamRegion()))
                .teamRatingAverage(reqTeamDto.getTeamRatingAverage())
                .recruitmentStatus(reqTeamDto.getRecruitmentStatus())                .teamIntroduction(reqTeamDto.getTeamIntroduction())
                .teamAttachmentEnabled(true)
                .build();
        Team savedTeam = teamBoardRepository.save(newTeam);

        for (String posName : reqTeamDto.getRecruitingPositions()) {

            PositionName enumValue = PositionName.valueOf(posName.trim());
            Positions position = positionsRepository.findByPositionName(enumValue)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid position name: " + posName));

            RecruitingPosition rp = RecruitingPosition.builder()
                    .team(savedTeam)
                    .positions(position)

                    .build();

            recruitingPositionRepository.save(rp);
        }

        insertFile(reqTeamDto.getTeamImageFile(), savedTeam);
    }

    private void insertFile(MultipartFile multipartFile, Team team) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            log.warn("⚠️ No image file uploaded for team '{}'", team.getTeamName());
            return;
        }

        String uuid = UUID.randomUUID().toString().replace("-", "");
        String ext = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
        String savedName = uuid + "." + ext;

        String teamDir = "attachments/team/";

        awsS3Utils.saveFile(teamDir, savedName, multipartFile); // uploads to correct key

        Attachment attachment = Attachment.builder()
                .boardType(BoardType.TEAM)
                .boardNumber(team.getId())
                .fileOrder(0)
                .originalName(multipartFile.getOriginalFilename())
                .savedName(savedName)
                .savePath(teamDir)
                .build();

        attachmentRepository.save(attachment);
    }

    public void updateFile(MultipartFile multipartFile, Team findTeamBoard){
        String fileName = UUID.randomUUID().toString().replace("-","");

        List<Attachment> findAttachments = attachmentRepository.findAllByBoardTypeAndBoardNumber(BoardType.TEAM, findTeamBoard.getId());
        if(findAttachments.isEmpty())
            throw new IllegalArgumentException(BoardType.TEAM+"타입, "+findTeamBoard.getId()+"번에는 첨부파일이 없습니다.");

        findAttachments.get(0).update(multipartFile.getOriginalFilename(), fileName+multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().indexOf(".")), FILE_DIR);

        attachmentRepository.save(findAttachments.get(0));

        awsS3Utils.deleteFile(FILE_DIR, fileName);

        awsS3Utils.saveFile(FILE_DIR, fileName, multipartFile);


    }

    public PageResponseDto<ResTeamDto> findAllWithPaging(
            PageRequest pageRequest,
            String recruitingPosition,
            String region,
            Double teamRatingAverage) {

        // Convert enums safely
        PositionName positionName = null;
        if (recruitingPosition != null && !recruitingPosition.isBlank()) {
            positionName = PositionName.valueOf(recruitingPosition.trim());
        }

        RegionType regionType = null;
        if (region != null && !region.isBlank()) {
            regionType = RegionType.valueOf(region.trim());
        }


        Page<Team> teamPage = teamBoardRepository.findTeamListWithPaging(
                positionName, regionType, teamRatingAverage, pageRequest);


        Page<ResTeamDto> dtoPage = teamPage.map(team -> {
            Optional<Attachment> attachment = attachmentRepository.findLatestAttachment(BoardType.TEAM, team.getId());
            String imageUrl = attachment
                    .map(att -> S3BaseUrl + "team/" + att.getSavedName())
                    .orElse("/img/default-team.png"); // fallback if no image

            return ResTeamDto.from(team, imageUrl);
        });

        return PageResponseDto.<ResTeamDto>builder()
                .items(dtoPage.getContent())
                .pageInfo(PageResponseDto.PageInfoDto.builder()
                        .page(dtoPage.getNumber())
                        .size(dtoPage.getNumberOfElements())
                        .totalElements(dtoPage.getTotalElements())
                        .totalPages(dtoPage.getTotalPages())
                        .isFirst(dtoPage.isFirst())
                        .isLast(dtoPage.isLast())
                        .build())
                .build();
    }
}
