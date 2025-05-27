package com.multi.matchon.stadium.repository;

import com.multi.matchon.stadium.domain.Stadium;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

public interface StadiumRepository extends JpaRepository<Stadium, Long> {

    // 경기, 서울, 강원, 충청, 전라, 제주, 경상, 세종이 포함된 DB를 필터별 조회...
    Page<Stadium> findByCpNmContaining(String cpNm, Pageable pageable);
    Page<Stadium> findAll(Pageable pageable); // 전체 조회 페이지 나누기(1페이지당 10개씩..)

}
