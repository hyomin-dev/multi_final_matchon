package com.multi.matchon.member;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.exception.custom.CustomException;
import com.multi.matchon.matchup.dto.req.ReqMatchupRatingDto;
import com.multi.matchon.matchup.service.MatchupRatingService;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.domain.MemberRole;
import com.multi.matchon.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MannerTempTest {

    @Autowired
    private MatchupRatingService ratingService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 동시성_테스트() throws Exception {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        Long targetId = 1L;
        Long boardId = 1L;
        Long evalId = 2L;

        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            executorService.execute(() -> {
                try {
                    ReqMatchupRatingDto dto = new ReqMatchupRatingDto();
                    dto.setBoardId(boardId);
                    dto.setEvalId(evalId+ finalI); // 평가자 동일
                    dto.setTargetId(targetId);
                    dto.setMannerScore(5); // 동일한 점수
                    dto.setSkillScore(5);
                    dto.setReview("테스트");

//                    Member evaluator = Member.builder()
//                            .id(evalId+ finalI)
//                            .memberEmail("eval@example.com")
//                            .memberPassword("encodedPassword")
//                            .memberName("평가자")
//                            .memberRole(MemberRole.USER)
//                            .myTemperature(36.5)
//                            .isDeleted(false)
//                            .team(null)
//                            .build();
                    Member evaluator = memberRepository.findById(evalId+ finalI).orElseThrow(()->new CustomException("회원이 존재하지 않습니다."));

                    CustomUser user = new CustomUser(evaluator);

                    ratingService.registerMatchupRating(dto, user);
                } catch (Exception e) {
                    System.out.println("예외 발생: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 쓰레드 종료될 때까지 대기

        // 🔎 최종 확인
        Member target = memberRepository.findById(targetId).orElseThrow();
        System.out.println("최종 매너온도: " + target.getMyTemperature());
    }
}