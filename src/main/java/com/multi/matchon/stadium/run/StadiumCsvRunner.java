package com.multi.matchon.stadium.run;

import com.multi.matchon.stadium.service.StadiumCsvService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//@Component
@RequiredArgsConstructor
public class StadiumCsvRunner implements CommandLineRunner {

    private final StadiumCsvService stadiumCsvService;

    @Override
    public void run(String... args) throws Exception {
        // 경로는 CSV 실제 위치에 따라 조정
        String path = "src/main/resources/stadium.csv";

        stadiumCsvService.importCsv(path);
        System.out.println("경기장 CSV 데이터 import 완료!");
    }
}
