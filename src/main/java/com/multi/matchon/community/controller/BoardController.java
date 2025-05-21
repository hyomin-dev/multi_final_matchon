package com.multi.matchon.community.controller;

import com.multi.matchon.community.domain.Board;
import com.multi.matchon.community.domain.Category;
import com.multi.matchon.community.domain.Comment;
import com.multi.matchon.community.dto.req.BoardRequest;
import com.multi.matchon.community.dto.req.CommentRequest;
import com.multi.matchon.community.service.BoardService;
import com.multi.matchon.community.service.CommentService;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.BindingResult;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final MemberService memberService;
    private final CommentService commentService;

    /**
     * 게시글 목록 조회
     * 선택된 카테고리를 기준으로 게시글을 조회하고, view에서 동적으로 제목 변경
     */
    @GetMapping
    public String listBy(@RequestParam(defaultValue = "FREEBOARD") Category category,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        Pageable pageable = PageRequest.of(page, 6, Sort.by("createdDate").descending());
        Page<Board> boardsPage = boardService.findByCategory(category, pageable);

        model.addAttribute("boardsPage", boardsPage);
        model.addAttribute("selectedCategory", category); // 동적 제목 변경용
        model.addAttribute("categories", Category.values());

        return "community/view"; // 목록 페이지
    }

    //게시글 상세 보기
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Board board = boardService.findById(id);

        List<String> savedPaths = board.getAttachmentPath() != null
                ? List.of(board.getAttachmentPath().split(";"))
                : List.of();

        List<String> originalNames = board.getAttachmentOriginalName() != null
                ? List.of(board.getAttachmentOriginalName().split(";"))
                : List.of();

        model.addAttribute("board", board);
        model.addAttribute("savedPaths", savedPaths);
        model.addAttribute("originalNames", originalNames);
        model.addAttribute("commentRequest", new CommentRequest());
        model.addAttribute("comments", commentService.getCommentsByBoard(board));

        return "community/detail";
    }


    //게시글 작성 폼
    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("boardRequest", new BoardRequest());
        model.addAttribute("categories", Category.values());
        return "community/form";
    }

    //게시글 저장 처리
    @PostMapping
    public String create(@Valid @ModelAttribute("boardRequest") BoardRequest boardRequest,
                         BindingResult bindingResult,
                         @RequestParam("files") MultipartFile[] files,
                         Model model) throws IOException {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", Category.values());
            return "community/form";
        }

        Member dummyMember = memberService.findById(1L);

        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        boolean hasAttachment = false;
        StringBuilder savedFileNames = new StringBuilder();
        StringBuilder originalFileNames = new StringBuilder();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String originalFilename = file.getOriginalFilename();
                String savedFileName = UUID.randomUUID() + "_" + originalFilename;
                file.transferTo(new File(uploadDir + savedFileName));

                savedFileNames.append(savedFileName).append(";");
                originalFileNames.append(originalFilename).append(";");
                hasAttachment = true;
            }
        }

        Board newBoard = Board.builder()
                .title(boardRequest.getTitle())
                .content(boardRequest.getContent())
                .category(boardRequest.getCategory())
                .member(dummyMember)
                .boardAttachmentEnabled(hasAttachment)
                .attachmentPath(savedFileNames.toString())
                .attachmentOriginalName(originalFileNames.toString())
                .build();

        boardService.save(newBoard);
        return "redirect:/community";
    }


    //댓글 작성 처리
    @PostMapping("/{id}/comments")
    public String addComment(@PathVariable Long id,
                             @Valid @ModelAttribute("commentRequest") CommentRequest commentRequest,
                             BindingResult bindingResult,
                             Model model) {
        Board board = boardService.findById(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("board", board);
            model.addAttribute("comments", commentService.getCommentsByBoard(board));
            return "community/detail";
        }

        Comment comment = Comment.builder()
                .board(board)
                .content(commentRequest.getContent())
                .build();

        commentService.save(comment);
        return "redirect:/community/" + id;
    }

    @GetMapping("/download/{filename}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) throws IOException {
        Path filePath = Paths.get(System.getProperty("user.dir"), "uploads", filename);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new FileNotFoundException("파일을 찾을 수 없습니다: " + filename);
        }

        String encodedFilename = UriUtils.encodePath(filename, StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFilename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(filePath))
                .body(resource);
    }

}

