package com.multi.matchon.community.controller;


import com.multi.matchon.community.domain.Board;
import com.multi.matchon.community.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/list")
    public String boardList(@RequestParam(defaultValue = "free") String category,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {
        int pageSize = 5;
        Page<Board> boardPage = boardService.getPagedPostsByCategory(category, page, pageSize);

        model.addAttribute("boards", boardPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", boardPage.getTotalPages());
        model.addAttribute("category", category);

        return "board/list";
    }

    @GetMapping("/write")
    public String writeForm() {
        return "board/write";
    }

    @PostMapping("/write")
    public String write(@RequestParam String title,
                        @RequestParam String content,
                        @RequestParam String author,
                        @RequestParam String category,
                        @RequestParam("files") MultipartFile[] files,
                        Model model) throws IOException {
        if (title.trim().isEmpty() || content.trim().isEmpty() || author.trim().isEmpty() || category.trim().isEmpty()) {
            model.addAttribute("error", "모든 필드를 입력해야 합니다.");
            model.addAttribute("title", title);
            model.addAttribute("content", content);
            model.addAttribute("author", author);
            model.addAttribute("category", category);
            return "board/write";
        }

        boardService.savePost(title, content, author, category, files);
        return "redirect:/list?category=" + category;
    }

    @GetMapping("/post/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Board board = boardService.getPost(id);
        model.addAttribute("board", board);
        return "board/detail";
    }

    @GetMapping("/download/{filename}")
    @ResponseBody
    public byte[] download(@PathVariable String filename) throws IOException {
        File file = new File("uploads", filename);
        return Files.readAllBytes(file.toPath());
    }

    @PostMapping("/post/{id}/delete-file")
    @ResponseBody
    public String deleteFile(@PathVariable Long id, @RequestParam String filename) throws IOException {
        boardService.deleteFileFromPost(id, filename);
        return "success";
    }
}



