package com.multi.matchon.matchup.service;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.dto.res.PageResponseDto;
import com.multi.matchon.common.exception.custom.ApiCustomException;
import com.multi.matchon.common.exception.custom.CustomException;
import com.multi.matchon.matchup.domain.MatchupBoard;
import com.multi.matchon.matchup.domain.MatchupRating;
import com.multi.matchon.matchup.domain.MatchupRequest;
import com.multi.matchon.matchup.dto.req.ReqMatchupRatingDto;
import com.multi.matchon.matchup.dto.res.ResMatchupMyGameListDto;
import com.multi.matchon.matchup.dto.res.ResMatchupRatingDto;
import com.multi.matchon.matchup.dto.res.ResMatchupRatingListDto;
import com.multi.matchon.matchup.repository.MatchupBoardRepository;
import com.multi.matchon.matchup.repository.MatchupRatingRepository;
import com.multi.matchon.matchup.repository.MatchupRequestRepository;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchupRatingService {


    private final MatchupRatingRepository matchupRatingRepository;
    private final MatchupRequestRepository matchupRequestRepository;
    private final MatchupBoardRepository matchupBoardRepository;
    private final MemberRepository memberRepository;


    /*
    * 내가 참여한 게임 목록 조회
    * */
    @Transactional(readOnly = true)
    public PageResponseDto<ResMatchupMyGameListDto> findAllMyGames(PageRequest pageRequest, CustomUser user) {

        Page<ResMatchupMyGameListDto> page = matchupRatingRepository.findAllMyGames(pageRequest, user.getMember());
        return PageResponseDto.<ResMatchupMyGameListDto>builder()
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

    /*
    * 작성자가 매너 온도 평가를 위한 matchup_rating table setting하는 메서드
    * 딱 1번만 할 수 있음
    * */
    @Transactional
    public void setMannerTemperatureSettingByBoardId(Long boardId, CustomUser user) {

        MatchupBoard ratingMatchupBoard = matchupBoardRepository.findByBoardIDAndMemberAndIsRatingInitializedFalse(boardId, user.getMember()).orElseThrow(()->new ApiCustomException("Matchup 매너 온도 평가 세팅할 게시글이 아닙니다."));

        List<MatchupRequest> matchupRequests = matchupRequestRepository.findByBoardIdAndMemberAndGameParticipantConditionAndAfterMatchupDatetime(boardId, user.getMember());
        log.info("test");

        List<MatchupRating> matchupRatings = new ArrayList<>();

        // evaluator: 작성자, target: 참가 요청자
        for(MatchupRequest mr: matchupRequests){
            MatchupRating matchupRating = MatchupRating.builder()
                    .matchupBoard(ratingMatchupBoard)
                    .memberEval(user.getMember())
                    .memberTarget(mr.getMember())
                    .build();

            matchupRatings.add(matchupRating);
        }

        // evaluator: 참가 요청자, target: 작성자

        for(MatchupRequest mr: matchupRequests){
            MatchupRating matchupRating = MatchupRating.builder()
                    .matchupBoard(ratingMatchupBoard)
                    .memberEval(mr.getMember())
                    .memberTarget(user.getMember())
                    .build();

            matchupRatings.add(matchupRating);
        }

        // evaluator: 참가 요청자, target: 참가 요청자

        for(MatchupRequest mr1:matchupRequests){
            for(MatchupRequest mr2: matchupRequests){
                if(mr1.getMember().getId().equals(mr2.getMember().getId()))
                    continue;
                MatchupRating matchupRating = MatchupRating.builder()
                        .matchupBoard(ratingMatchupBoard)
                        .memberEval(mr1.getMember())
                        .memberTarget(mr2.getMember())
                        .build();
                matchupRatings.add(matchupRating);
            }
        }

        matchupRatingRepository.saveAll(matchupRatings);

        ratingMatchupBoard.setRatingInitialized(true);

    }


    /*
    *한 경기에서 내가 평가할 목록을 반환, 페이징처리
    * */
    @Transactional(readOnly = true)
    public PageResponseDto<ResMatchupRatingListDto> findAllMyRatings(PageRequest pageRequest, CustomUser user, Long boardId) {

        Page<ResMatchupRatingListDto> page = matchupRatingRepository.findAllMyRatings(pageRequest, user.getMember(), boardId);
        return PageResponseDto.<ResMatchupRatingListDto>builder()
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

    /*
    * 평가할 대상이 있는 지 조회
    * */
    @Transactional(readOnly = true)
    public ResMatchupRatingDto findResMatchupRatingDto(Long boardId, Long evalId, Long targetId, CustomUser user) {

        if(!evalId.equals(user.getMember().getId()))
            throw new CustomException("Matchup 매너온도 평가를 할 권한이 없습니다.");

       return matchupRatingRepository.findResMatchupRatingDtoByBoardIdAndEvalIdAndTargetId(boardId, evalId, targetId).orElseThrow(()->new CustomException("Matchup 이미 평가를 하셨습니다."));

    }

    /*
    * 매너 온도 평가 등록, 마이페이지 업데이트
    * */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void registerMatchupRating(@Valid ReqMatchupRatingDto reqMatchupRatingDto, CustomUser user) {

        if(!reqMatchupRatingDto.getEvalId().equals(user.getMember().getId()))
            throw new CustomException("Matchup 매너온도 평가를 할 권한이 없습니다.");

        MatchupRating matchupRating = matchupRatingRepository.findByBoardIdAndEvalIdAndTargetId(reqMatchupRatingDto.getBoardId(), reqMatchupRatingDto.getEvalId(), reqMatchupRatingDto.getTargetId()).orElseThrow(()->new CustomException("Matchup 이미 평가를 하셨습니다."));

        matchupRating.rating(reqMatchupRatingDto.getMannerScore(), reqMatchupRatingDto.getSkillScore(), reqMatchupRatingDto.getReview(), true);

        Member member =  memberRepository.findByIdAndIsDeletedFalseWithLock(reqMatchupRatingDto.getTargetId()).orElseThrow(()->new CustomException("Matchup 평가 대상 회원이 존재하지 않습니다."));

//        Double sumScoreWithSigmoid = sigmoid((reqMatchupRatingDto.getMannerScore()+ reqMatchupRatingDto.getSkillScore())/10.0);

        Double changeTemp = member.getMyTemperature() + (reqMatchupRatingDto.getMannerScore()*0.14+ reqMatchupRatingDto.getSkillScore()*0.06 -0.4) *0.01;

        member.updateMyTemperature(changeTemp);



    }

    public static double sigmoid(double x){
        return 1.0 / (1.0+Math.exp(-x));
    }

    public ResMatchupRatingDto findDetailResMatchupRatingDto(Long boardId, Long evalId, Long targetId) {

        return matchupRatingRepository.findDetailResMatchupRatingDtoByBoardIdAndEvalIdAndTargetId(boardId, evalId, targetId).orElseThrow(() ->new CustomException("Matchup 등록된 평가가 없습니다."));
    }
}
