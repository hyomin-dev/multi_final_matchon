package com.multi.matchon.team.service;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.team.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.multi.matchon.common.domain.*;

import com.multi.matchon.common.dto.res.PageResponseDto;
import com.multi.matchon.common.repository.AttachmentRepository;
import com.multi.matchon.common.repository.PositionsRepository;
import com.multi.matchon.common.repository.SportsTypeRepository;

import com.multi.matchon.common.util.AwsS3Utils;
import com.multi.matchon.matchup.domain.MatchupBoard;
import com.multi.matchon.matchup.dto.res.ResMatchupBoardListDto;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.repository.MemberRepository;
import com.multi.matchon.team.domain.*;
import com.multi.matchon.team.dto.req.ReqReviewDto;
import com.multi.matchon.team.dto.req.ReqTeamDto;
import com.multi.matchon.team.dto.req.ReqTeamJoinDto;
import com.multi.matchon.team.dto.res.ResReviewDto;
import com.multi.matchon.team.dto.res.ResTeamDto;
import com.multi.matchon.team.repository.RecruitingPositionRepository;
import com.multi.matchon.team.repository.ReviewRepository;
import com.multi.matchon.team.repository.TeamMemberRepository;
import com.multi.matchon.team.repository.TeamNameRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import java.util.stream.Collectors;

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


    private final TeamNameRepository teamRepository;

    private final RecruitingPositionRepository recruitingPositionRepository;

    private final PositionsRepository positionsRepository;
    private final AttachmentRepository attachmentRepository;

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final TeamMemberRepository teamMemberRepository;

    @PersistenceContext
    private EntityManager em;

    private final AwsS3Utils awsS3Utils;


    public List<Team> findAll() {
        List<Team> teamBoards = teamRepository.findAll();


        return teamBoards;
    }


    public void teamRegister(ReqTeamDto reqTeamDto, CustomUser user) {
        // Check if the user already has an active team
        if (teamRepository.existsByCreatedPersonAndIsDeletedFalse(user.getUsername())) {
            throw new IllegalArgumentException("이미 팀이 있습니다.");
        }
        Team newTeam = Team.builder()
                .teamName(reqTeamDto.getTeamName())
                .teamRegion(RegionType.valueOf(reqTeamDto.getTeamRegion()))
                .teamRatingAverage(reqTeamDto.getTeamRatingAverage())
                .recruitmentStatus(reqTeamDto.getRecruitmentStatus()).teamIntroduction(reqTeamDto.getTeamIntroduction())
                .teamAttachmentEnabled(true)
                .build();
        Team savedTeam = teamRepository.save(newTeam);

        // Add creator as team member (leader)
        Member member = memberRepository.findByMemberEmail(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다: " + user.getUsername()));

        TeamMember teamMember = TeamMember.builder()
                .team(savedTeam)
                .member(member)
                .introduction("팀 리더입니다.")
                .teamLeaderStatus(true)
                .build();

        teamMemberRepository.save(teamMember);

        em.createQuery("UPDATE Member m SET m.team = :team WHERE m.memberEmail = :email")
                .setParameter("team", savedTeam)
                .setParameter("email", user.getUsername())
                .executeUpdate();


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

    public void updateFile(MultipartFile multipartFile, Team findTeamBoard) {
        String fileName = UUID.randomUUID().toString().replace("-", "");

        List<Attachment> findAttachments = attachmentRepository.findAllByBoardTypeAndBoardNumber(BoardType.TEAM, findTeamBoard.getId());
        if (findAttachments.isEmpty())
            throw new IllegalArgumentException(BoardType.TEAM + "타입, " + findTeamBoard.getId() + "번에는 첨부파일이 없습니다.");

        findAttachments.get(0).update(multipartFile.getOriginalFilename(), fileName + multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().indexOf(".")), FILE_DIR);

        attachmentRepository.save(findAttachments.get(0));

        awsS3Utils.deleteFile(FILE_DIR, fileName);

        awsS3Utils.saveFile(FILE_DIR, fileName, multipartFile);



    }

    public PageResponseDto<ResTeamDto> findAllWithPaging(
            PageRequest pageRequest,
            String recruitingPosition,
            String region,
            Double teamRatingAverage) {

        log.info("📌 teamRatingAverage = {}", teamRatingAverage);

        // ✅ Convert enums safely
        PositionName positionName = null;
        if (recruitingPosition != null && !recruitingPosition.isBlank()) {
            positionName = PositionName.valueOf(recruitingPosition.trim());
        }

        RegionType regionType = null;
        if (region != null && !region.isBlank()) {
            regionType = RegionType.valueOf(region.trim());
        }

        // ✅ 🔀 Use correct query based on presence of rating filter
        Page<Team> teamPage;
        if (teamRatingAverage == null) {
            log.info("📤 Calling findWithoutRatingFilter() — no 별점 filter");
            teamPage = teamRepository.findWithoutRatingFilter(positionName, regionType, pageRequest);
        } else {
            log.info("📤 Calling findWithRatingFilter() — rating filter = {}", teamRatingAverage);
            teamPage = teamRepository.findWithRatingFilter(positionName, regionType, teamRatingAverage, pageRequest);
        }

        // ✅ Transform to DTOs with image handling
        Page<ResTeamDto> dtoPage = teamPage.map(team -> {
            Optional<Attachment> attachment = attachmentRepository.findLatestAttachment(BoardType.TEAM, team.getId());
            String imageUrl = attachment
                    .map(att -> S3BaseUrl + "team/" + att.getSavedName())
                    .orElse("/img/default-team.png");

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


    public ResTeamDto findTeamById(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다: " + teamId));

        Optional<Attachment> attachment = attachmentRepository.findLatestAttachment(BoardType.TEAM, team.getId());
        String imageUrl = attachment
                .map(att -> S3BaseUrl + "team/" + att.getSavedName())
                .orElse("/img/default-team.png");

        return ResTeamDto.from(team, imageUrl);
    }

    @Transactional
    public void processTeamJoinRequest(Long teamId, ReqTeamJoinDto joinRequest, CustomUser user) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다: " + teamId));

        if (!team.getRecruitmentStatus()) {
            throw new IllegalArgumentException("현재 팀원 모집이 진행중이지 않습니다.");
        }

        Member member = memberRepository.findByMemberEmail(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다: " + user.getUsername()));

        if (member.getTeam() != null) {
            throw new IllegalArgumentException("이미 다른 팀에 소속되어 있습니다.");
        }

        // Check if position is valid
        boolean isValidPosition = team.getRecruitingPositions().stream()
                .anyMatch(rp -> rp.getPositions().getPositionName().name().equals(joinRequest.getPosition()));

        if (!isValidPosition) {
            throw new IllegalArgumentException("선택한 포지션이 팀의 모집 포지션과 일치하지 않습니다.");
        }

        // Create team member with pending status
        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .member(member)
                .introduction(joinRequest.getIntroduction())
                .teamLeaderStatus(false)
                .build();

        teamMemberRepository.save(teamMember);
    }

    @Transactional
    public void saveReview(Long teamId, CustomUser user, ReqReviewDto dto) {
        log.info("Attempting to save review for team {} by user {}", teamId, user.getUsername());
        
        // Find the member who is writing the review
        Member member = memberRepository.findByMemberEmail(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));
        log.info("Found member with ID: {}", member.getId());

        // Find the team
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다: " + teamId));
        // Create and save the review
        Review review = Review.builder()
                .member(member)
                .team(team)
                .reviewRating(dto.getRating())
                .content(dto.getContent())
                .isDeleted(false)
                .build();

        // Save the review
        Review savedReview = reviewRepository.save(review);
        log.info("Successfully saved review with ID: {}", savedReview.getId());
    }

    @Transactional(readOnly = true)
    public List<ResReviewDto> getReviewsForTeam(Long teamId) {
        return reviewRepository.findReviewsByTeamId(teamId)
                .stream()
                .map(ResReviewDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateReview(Long reviewId, CustomUser user, ReqReviewDto dto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        // Check if the user is the owner of the review
        if (!review.getMember().getMemberEmail().equals(user.getUsername())) {
            throw new IllegalArgumentException("리뷰 작성자만 수정할 수 있습니다.");
        }

        // Update review
        review.updateReview(dto.getRating(), dto.getContent());
    }

    @Transactional
    public void deleteReview(Long reviewId, CustomUser user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        // Check if the user is the owner of the review
        if (!review.getMember().getMemberEmail().equals(user.getUsername())) {
            throw new IllegalArgumentException("리뷰 작성자만 삭제할 수 있습니다.");
        }

        // Soft delete the review
        review.softDelete();
    }

    @Transactional(readOnly = true)
    public List<ResReviewDto> getMyReviewsForTeam(Long teamId, String userEmail) {
        return reviewRepository.findReviewsByTeamId(teamId).stream()
                .filter(r -> r.getMember().getMemberEmail().equals(userEmail))
                .map(ResReviewDto::from)
                .collect(Collectors.toList());
    }

}

//    public PageResponseDto<ResTeamDto> findAllWithPaging(
//            PageRequest pageRequest,
//            String recruitingPosition,
//            String region,
//            Double teamRatingAverage) {
//
//        // Convert enums safely
//        PositionName positionName = null;
//        if (recruitingPosition != null && !recruitingPosition.isBlank()) {
//            positionName = PositionName.valueOf(recruitingPosition.trim());
//        }
//
//        RegionType regionType = null;
//        if (region != null && !region.isBlank()) {
//            regionType = RegionType.valueOf(region.trim());
//        }
//
//

//        Page<Team> teamPage = teamRepository.findTeamListWithPaging(

//                positionName, regionType, teamRatingAverage, pageRequest);
//
//
//        Page<ResTeamDto> dtoPage = teamPage.map(ResTeamDto::from);
//
//        return PageResponseDto.<ResTeamDto>builder()
//                .items(dtoPage.getContent())
//                .pageInfo(PageResponseDto.PageInfoDto.builder()
//                        .page(dtoPage.getNumber())
//                        .size(dtoPage.getNumberOfElements())
//                        .totalElements(dtoPage.getTotalElements())
//                        .totalPages(dtoPage.getTotalPages())
//                        .isFirst(dtoPage.isFirst())
//                        .isLast(dtoPage.isLast())
//                        .build())
//                .build();
//    }

//}

