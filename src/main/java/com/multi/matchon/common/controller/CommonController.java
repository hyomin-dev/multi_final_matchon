package com.multi.matchon.common.controller;

import com.multi.matchon.common.dto.res.SportsTypeDto;
import com.multi.matchon.common.service.CommonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommonController {

    private final CommonService commonService;

    @GetMapping("/sports-types")
    public ResponseEntity<List<SportsTypeDto>> findAllSportsType(){
        List<SportsTypeDto> sportsTypeDtos = commonService.findAllSportsType();

        return ResponseEntity.ok(sportsTypeDtos);

    }
}
