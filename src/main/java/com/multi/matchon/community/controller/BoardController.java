package com.multi.matchon.community.controller;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.community.domain.Board;
import com.multi.matchon.community.domain.Category;
import com.multi.matchon.community.domain.Comment;
import com.multi.matchon.community.dto.req.BoardRequest;
import com.multi.matchon.community.dto.req.CommentRequest;
import com.multi.matchon.community.service.BoardService;
import com.multi.matchon.community.service.CommentService;
import com.multi.matchon.community.service.MemberDetails;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.BindingResult;
import org.springframework.web.util.UriUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

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

    @GetMapping
    public String listBy(@RequestParam(defaultValue = "FREEBOARD") Category category,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        Pageable pageable = PageRequest.of(page, 6, Sort.by("createdDate").descending());
        Page<Board> boardsPage = boardService.findByCategory(category, pageable);

        model.addAttribute("boardsPage", boardsPage);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", Category.values());

        return "community/view";
    }

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

    @GetMapping("/new")
    public String form(Model model, @AuthenticationPrincipal CustomUser userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        model.addAttribute("boardRequest", new BoardRequest());
        model.addAttribute("categories", Category.values());
        model.addAttribute("memberName", userDetails.getMember().getMemberName()); // ← 여기 주의!

        return "community/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("boardRequest") BoardRequest boardRequest,
                         BindingResult bindingResult,
                         @RequestParam("files") MultipartFile[] files,
                         Model model,
                         @AuthenticationPrincipal CustomUser userDetails) throws IOException {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", Category.values());
            return "community/form";
        }


        Member loginMember = userDetails.getMember();

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
                .member(loginMember)
                .boardAttachmentEnabled(hasAttachment)
                .attachmentPath(savedFileNames.toString())
                .attachmentOriginalName(originalFileNames.toString())
                .build();

        boardService.save(newBoard);
        return "redirect:/community";
    }


    @PostMapping("/{id}/comments")
    public String addComment(@PathVariable Long id,
                             @Valid @ModelAttribute("commentRequest") CommentRequest commentRequest,
                             BindingResult bindingResult,
                             Model model,
                             @AuthenticationPrincipal CustomUser userDetails) {
        Board board = boardService.findById(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("board", board);
            model.addAttribute("comments", commentService.getCommentsByBoard(board));
            return "community/detail";
        }

        Comment comment = Comment.builder()
                .board(board)
                .member(userDetails.getMember()) // 작성자 정보 설정
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


    @PostMapping("/{boardId}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long boardId,
                                @PathVariable Long commentId,
                                @AuthenticationPrincipal CustomUser userDetails) {
        Comment comment = commentService.findById(commentId);

        // 본인 확인
        if (!comment.getMember().getId().equals(userDetails.getMember().getId())) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        commentService.softDelete(commentId); // 논리 삭제
        return "redirect:/community/" + boardId;
    }



}
