package com.multi.matchon.stadium.controller;

import com.multi.matchon.stadium.domain.Stadium;
import com.multi.matchon.stadium.repository.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stadiums")
@RequiredArgsConstructor
public class StadiumApiController {

    private final StadiumRepository stadiumRepository;

    @GetMapping
    public List<Stadium> getAllStadiums() {
        return stadiumRepository.findAll();
    }
}
