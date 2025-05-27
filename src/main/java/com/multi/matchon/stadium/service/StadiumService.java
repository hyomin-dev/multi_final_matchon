package com.multi.matchon.stadium.service;

import com.multi.matchon.stadium.domain.Stadium;
import com.multi.matchon.stadium.repository.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StadiumService {

    private final StadiumRepository stadiumRepository;

    // 전체 경기장 목록 조회
    public List<Stadium> getAllStadiums() {
        return stadiumRepository.findAll();
    }

    // 지역별로 경기장 필터링
    public List<Stadium> getStadiumsByRegion(String region) {
        return stadiumRepository.findByCpNmContaining(region);
    }
}
