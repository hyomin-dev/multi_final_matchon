package com.multi.matchon.community.controller;

import com.multi.matchon.common.auth.dto.CustomUser;

import com.multi.matchon.common.dto.UploadedFile;
import com.multi.matchon.common.util.AwsS3Utils;
import com.multi.matchon.common.util.FileUploadHelper;
import com.multi.matchon.community.domain.Board;
import com.multi.matchon.community.domain.Category;
import com.multi.matchon.community.dto.req.BoardRequest;
import com.multi.matchon.community.dto.req.CommentRequest;
import com.multi.matchon.community.service.BoardService;
import com.multi.matchon.community.service.CommentService;
import com.multi.matchon.community.service.ReportService;
import com.multi.matchon.member.domain.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.awspring.cloud.s3.S3Resource;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class BoardController {

    private static final String COMMUNITY_DIR = "community/";
    private static final String IMAGE_DIR = "community/images/";
    private static final String FORM_VIEW = "community/form";

    private final BoardService boardService;
    private final CommentService commentService;
    private final ReportService reportService;

    private final AwsS3Utils awsS3Utils;

    @GetMapping
    public String list(@RequestParam(defaultValue = "FREEBOARD") Category category,
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

        List<String> savedPaths = Optional.ofNullable(board.getAttachmentPath())
                .map(path -> List.of(path.split(";")))
                .orElse(List.of());

        List<String> originalNames = Optional.ofNullable(board.getAttachmentOriginalName())
                .map(name -> List.of(name.split(";")))
                .orElse(List.of());

        model.addAttribute("board", board);
        model.addAttribute("savedPaths", savedPaths);
        model.addAttribute("originalNames", originalNames);
        model.addAttribute("commentRequest", new CommentRequest());
        model.addAttribute("comments", commentService.getCommentsByBoard(board));
        return "community/detail";
    }

    @GetMapping("/new")
    public String form(Model model, @AuthenticationPrincipal CustomUser user) {
        if (user == null) return "redirect:/login";

        model.addAttribute("boardRequest", new BoardRequest());
        model.addAttribute("categories", Category.values());
        model.addAttribute("memberName", user.getMember().getMemberName());
        model.addAttribute("formAction", "/community");
        return FORM_VIEW;
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("boardRequest") BoardRequest boardRequest,
                         BindingResult bindingResult,
                         @RequestParam("files") MultipartFile[] files,
                         Model model,
                         @AuthenticationPrincipal CustomUser user) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", Category.values());
            return FORM_VIEW;
        }

        Member member = user.getMember();
        StringBuilder saved = new StringBuilder();
        StringBuilder original = new StringBuilder();
        boolean hasAttachment = false;

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                UploadedFile uploaded = FileUploadHelper.uploadToS3(file, COMMUNITY_DIR, awsS3Utils);
                saved.append(uploaded.getSavedFileName()).append(";");
                original.append(uploaded.getOriginalFileName()).append(";");
                hasAttachment = true;
            }
        }

        Board board = Board.builder()
                .title(boardRequest.getTitle())
                .content(boardRequest.getContent())
                .category(boardRequest.getCategory())
                .member(member)
                .boardAttachmentEnabled(hasAttachment)
                .attachmentPath(saved.toString())
                .attachmentOriginalName(original.toString())
                .build();

        boardService.save(board);
        return "redirect:/community";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, @AuthenticationPrincipal CustomUser user) {
        Board board = boardService.findById(id);
        if (!board.getMember().getId().equals(user.getMember().getId())) {
            return "redirect:/community";
        }

        BoardRequest request = new BoardRequest(board.getTitle(), board.getContent(), board.getCategory());

        model.addAttribute("boardRequest", request);
        model.addAttribute("categories", Category.values());
        model.addAttribute("memberName", user.getMember().getMemberName());
        model.addAttribute("formAction", "/community/" + id + "/edit");
        model.addAttribute("boardId", id);

        return FORM_VIEW;
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("boardRequest") BoardRequest boardRequest,
                         BindingResult bindingResult,
                         @RequestParam("files") MultipartFile[] files,
                         Model model,
                         @AuthenticationPrincipal CustomUser user) throws IOException {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", Category.values());
            model.addAttribute("memberName", user.getMember().getMemberName());
            model.addAttribute("formAction", "/community/" + id + "/edit");
            model.addAttribute("boardId", id);
            return FORM_VIEW;
        }

        Board board = boardService.findById(id);
        if (!board.getMember().getId().equals(user.getMember().getId())) {
            return "redirect:/community";
        }

        StringBuilder saved = new StringBuilder();
        StringBuilder original = new StringBuilder();
        boolean hasAttachment = false;

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                UploadedFile uploaded = FileUploadHelper.uploadToS3(file, COMMUNITY_DIR, awsS3Utils);
                saved.append(uploaded.getSavedFileName()).append(";");
                original.append(uploaded.getOriginalFileName()).append(";");
                hasAttachment = true;
            }
        }

        board.update(
                boardRequest.getTitle(),
                boardRequest.getContent(),
                boardRequest.getCategory(),
                hasAttachment ? saved.toString() : board.getAttachmentPath(),
                hasAttachment ? original.toString() : board.getAttachmentOriginalName()
        );

        boardService.save(board);
        return "redirect:/community/" + id;
    }

    @GetMapping("/download/{filename}")
    public String downloadRedirect(@PathVariable String filename) {
        String saved = boardService.findSavedFilenameByPartialName(filename);
        return saved == null ? "redirect:/community" : "redirect:" + awsS3Utils.createPresignedGetUrl(COMMUNITY_DIR, saved);
    }

    @GetMapping("/download-force/{filename}")
    public ResponseEntity<Resource> forceDownload(@PathVariable String filename) throws IOException {
        Board board = boardService.findByAttachmentFilename(filename);
        if (board == null) return ResponseEntity.notFound().build();

        String[] paths = board.getAttachmentPath().split(";");
        String[] names = board.getAttachmentOriginalName().split(";");

        String originalName = filename;
        for (int i = 0; i < paths.length; i++) {
            if (paths[i].equals(filename)) {
                originalName = names[i];
                break;
            }
        }

        S3Resource resource = awsS3Utils.downloadFileWithFullName(COMMUNITY_DIR, filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + URLEncoder.encode(originalName, StandardCharsets.UTF_8) + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                .body(resource);
    }

    @PostMapping("/image-upload")
    @ResponseBody
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile image) {
        if (image.isEmpty()) return ResponseEntity.badRequest().body("No file selected");

        String uuidFileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        awsS3Utils.saveFile(IMAGE_DIR, uuidFileName, image);
        String imageUrl = awsS3Utils.getObjectUrl(IMAGE_DIR, uuidFileName, image);

        return ResponseEntity.ok(Map.of("url", imageUrl));
    }

    @PostMapping("/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> deletePost(@PathVariable Long id, @AuthenticationPrincipal CustomUser user) {
        if (user == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        boardService.deleteByIdAndUser(id, user.getMember());
        return ResponseEntity.ok().build();
    }


}
