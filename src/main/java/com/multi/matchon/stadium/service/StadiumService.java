package com.multi.matchon.stadium.service;

import com.multi.matchon.stadium.domain.Stadium;
import com.multi.matchon.stadium.repository.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class StadiumService {

    private final StadiumRepository stadiumRepository;

    // 전체 경기장 목록 조회
    public Page<Stadium> getAllStadiums(Pageable pageable) {
        return stadiumRepository.findAll(pageable);
    }

    // 지역별로 경기장 필터링
    public Page<Stadium> getStadiumsByRegion(String region, Pageable pageable) {
        return stadiumRepository.findByCpNmContaining(region, pageable);
    }
}
