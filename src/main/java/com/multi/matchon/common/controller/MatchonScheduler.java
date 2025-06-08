package com.multi.matchon.common.controller;


import com.multi.matchon.matchup.service.MatchupBoardService;
import com.multi.matchon.matchup.service.MatchupRatingService;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class MatchonScheduler {
    private final MatchupBoardService matchupBoardService;
    private int count1=1;
    private int count2=1;
    private final MemberRepository memberRepository;
    private final MatchupRatingService matchupRatingService;


    //@Scheduled(fixedDelay=3000)
    public void test1(){
        Member m = memberRepository.findById(3L).get();
        log.info("member temperature{}:{} ",count1++, m.getMyTemperature());
        //log.info("test1-count : {}",count1++);
    }

    /*
    * 경기 종료 후 작성자와 참가자들이 서로서로에 대해서 매너온도 평가를 할 수 있도록 자동 세팅
    * */
    @Scheduled(cron="0 * * * * ?")
    public void setMannerTemperatureAutoSetting(){

        Integer result = matchupRatingService.setMannerTemperatureAutoSetting();
        log.info("자동 평가 세팅 갯수: {}", result);
    }

//    @Scheduled(cron="0 * * * * ?")
//    public void setMatchNotificationBeforeStart(){
//        Integer result = matchupBoardService.notifyAllParticipantsBeforeStart();
//    }
}
