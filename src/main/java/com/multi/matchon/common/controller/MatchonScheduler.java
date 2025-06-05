package com.multi.matchon.common.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MatchonScheduler {
    private int count1=1;
    private int count2=1;


    //@Scheduled(fixedDelay=1000)
    public void test1(){
        log.info("test1-count : {}",count1++);
    }

    //@Scheduled(fixedDelay=2000)
    public void test2(){
        log.info("test2-count : {}",count2++);
    }
}
