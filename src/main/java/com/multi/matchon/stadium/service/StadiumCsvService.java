package com.multi.matchon.stadium.service;

import com.multi.matchon.stadium.domain.Stadium;
import com.multi.matchon.stadium.repository.StadiumRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StadiumCsvService {

    private final StadiumRepository stadiumRepository;

    public void importCsv(String filePath) throws IOException {
        try (
                Reader reader = Files.newBufferedReader(Paths.get(filePath));
                CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build()
        ) {
            List<String[]> records;
            try {
                records = csvReader.readAll();
            } catch (CsvException e) {
                throw new RuntimeException("CSV 파싱 중 오류 발생", e);
            }

            for (String[] row : records) {
                // 빈 값 체 (예 : 이름, 지역, 주소, 전화번호는 필수)
                if (row[1].isBlank() || row[3].isBlank() || row[5].isBlank() || row[24].isBlank()) {
                    continue;
                }

                Stadium stadium = Stadium.builder()
                        .faciNm(row[1])
                        .cpNm(row[3])
                        .faciRoadAddr(row[5])
                        .faciTelNo(row[24])
                        .isDeleted(false)
                        .build();

                stadiumRepository.save(stadium);
            }
        }
    }
}