package com.multi.matchon.stadium.repository;

import com.multi.matchon.stadium.domain.Stadium;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StadiumRepository extends JpaRepository<Stadium, Long> {

    // 경기, 서울, 강원, 충청, 전라, 제주, 경상이 포함된 DB를 필터별 조회...
    List<Stadium> findByCpNmContaining(String cpNm);
}
