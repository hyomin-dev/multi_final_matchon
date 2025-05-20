package com.multi.matchon.community.service;


import com.multi.matchon.community.domain.Board;
import com.multi.matchon.community.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public void savePost(String title, String content, String author, String category, MultipartFile[] files) throws IOException {
        Board board = new Board();
        board.setTitle(title);
        board.setContent(content);
        board.setAuthor(author);
        board.setCategory(category);
        board.setCreatedAt(LocalDateTime.now());

        File directory = new File(uploadDir);
        if (!directory.exists()) directory.mkdirs();

        StringBuilder savedFilenames = new StringBuilder();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String uniqueFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                File destFile = new File(directory, uniqueFilename);
                file.transferTo(destFile);

                if (savedFilenames.length() > 0) savedFilenames.append(",");
                savedFilenames.append(uniqueFilename);
            }
        }

        board.setFilename(savedFilenames.toString());
        boardRepository.save(board);
    }

    public Page<Board> getPagedPostsByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Board> boardPage = boardRepository.findByCategory(category, pageable);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        boardPage.getContent().forEach(board -> {
            if (board.getCreatedAt() != null) {
                board.setFormattedCreatedAt(board.getCreatedAt().format(formatter));
            }
        });

        return boardPage;
    }

    public Board getPost(Long id) {
        return boardRepository.findById(id).orElse(null);
    }

    public void deleteFileFromPost(Long postId, String filenameToDelete) throws IOException {
        Board board = boardRepository.findById(postId).orElse(null);
        if (board == null || board.getFilename() == null) return;

        File file = new File(uploadDir, filenameToDelete);
        if (file.exists()) file.delete();

        String[] filenames = board.getFilename().split(",");
        String updatedFilenames = Arrays.stream(filenames)
                .filter(name -> !name.equals(filenameToDelete))
                .collect(Collectors.joining(","));

        board.setFilename(updatedFilenames);
        boardRepository.save(board);
    }
}
