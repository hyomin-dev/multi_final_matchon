package com.multi.matchon.common.controller;


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

    @Scheduled(cron="0 * * * * ?")
    public void setMannerTemperatureAutoSetting(){

        Integer result = matchupRatingService.setMannerTemperatureAutoSetting();
        log.info("자동 평가 세팅 갯수: {}", result);
    }
}
