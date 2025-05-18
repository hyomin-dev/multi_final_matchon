package com.multi.matchon.matchup.service;

import com.multi.matchon.common.util.AwsS3Utils;
import com.multi.matchon.matchup.domain.MatchupBoard;
import com.multi.matchon.matchup.dto.req.ReqMatchupBoardDto;
import com.multi.matchon.matchup.repository.MatchupBoardRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MatchupService{

    @Value("${spring.cloud.aws.s3.base-url}")
    private String S3_URL;
    private String FILE_DIR = "attachments/";
    private String FILE_URL;
    @PostConstruct
    public void init(){
        this.FILE_URL = S3_URL;
    }


    private final MatchupBoardRepository matchupBoardRepository;
    private final AwsS3Utils awsS3Utils;



    public List<MatchupBoard> findAll() {
        List<MatchupBoard> matchupBoards = matchupBoardRepository.findAll();
        List<MatchupBoard> matchupBoards1 = matchupBoardRepository.findAllWithMember();
        List<MatchupBoard> matchupBoards2 = matchupBoardRepository.findAllWithMemberAndWithSportsType();

        return matchupBoards;
    }

    public void boardRegister(ReqMatchupBoardDto reqMatchupBoardDto) {
        insertFile(reqMatchupBoardDto);
        log.info("대기");
    }

    public void insertFile(ReqMatchupBoardDto reqMatchupBoardDto){
        String fileName = UUID.randomUUID().toString().replace("-","");
        awsS3Utils.saveFile(FILE_DIR, fileName, reqMatchupBoardDto.getReservationFile());
    }

    //public void
}
