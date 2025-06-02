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

    public Page<Stadium> getAllStadiums(Pageable pageable) {
        return stadiumRepository.findAll(pageable);
    }

    public Page<Stadium> searchStadiumsByName(String keyword, Pageable pageable) {
        return stadiumRepository.findByStadiumNameContainingIgnoreCase(keyword, pageable);
    }

    public Page<Stadium> filterStadiumsByRegion(String region, Pageable pageable) {
        return stadiumRepository.findByStadiumRegionContaining(region, pageable);
    }

    public Stadium getStadiumById(Long id) {
        return stadiumRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 구장이 존재하지 않습니다."));
    }
}
