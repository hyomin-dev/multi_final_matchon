package com.multi.matchon.community.controller;

import com.multi.matchon.community.domain.Board;
import com.multi.matchon.community.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("boards", boardService.findAll());
        return "community/view";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("board", boardService.findById(id));
        return "community/detail";
    }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("board", new Board());
        return "community/form";
    }

    @PostMapping
    public String create(@ModelAttribute Board board,
                         @RequestParam("file") MultipartFile file) throws IOException {

        if (!file.isEmpty()) {
            String uploadDir = "uploads/";
            String originalFilename = file.getOriginalFilename();
            String savedFileName = UUID.randomUUID() + "_" + originalFilename;

            File destFile = new File(uploadDir + savedFileName);
            file.transferTo(destFile);

            board = Board.builder()
                    .title(board.getTitle())
                    .content(board.getContent())
                    .category(board.getCategory())
                    .member(board.getMember()) // Security 적용 시 현재 로그인 사용자로 설정 필요
                    .boardAttachmentEnabled(true)
                    .attachmentPath(savedFileName)
                    .attachmentOriginalName(originalFilename)
                    .build();
        }

        boardService.save(board);
        return "redirect:/community";
    }
}





