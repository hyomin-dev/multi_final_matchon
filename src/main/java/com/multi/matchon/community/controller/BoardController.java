package com.multi.matchon.community.controller;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.util.AwsS3Utils;
import com.multi.matchon.community.domain.Board;
import com.multi.matchon.community.domain.Category;
import com.multi.matchon.community.dto.req.BoardRequest;
import com.multi.matchon.community.dto.req.CommentRequest;
import com.multi.matchon.community.service.BoardService;
import com.multi.matchon.community.service.CommentService;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.service.MemberService;
import io.awspring.cloud.s3.S3Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final MemberService memberService;
    private final CommentService commentService;
    private final AwsS3Utils awsS3Utils;

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
        model.addAttribute("memberName", userDetails.getMember().getMemberName());
        model.addAttribute("formAction", "/community");

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
        String dirName = "community/";

        boolean hasAttachment = false;
        StringBuilder savedFileNames = new StringBuilder();
        StringBuilder originalFileNames = new StringBuilder();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String originalFilename = file.getOriginalFilename();
                String extension = FilenameUtils.getExtension(originalFilename);
                String baseName = FilenameUtils.getBaseName(originalFilename);

                String uuidFileName = UUID.randomUUID() + "_" + baseName; // 확장자 없는 파일명
                String fullFileName = uuidFileName + "." + extension;

                awsS3Utils.saveFile(dirName, uuidFileName, file); // 확장자 없이 전달

                savedFileNames.append(fullFileName).append(";");
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

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model,
                           @AuthenticationPrincipal CustomUser userDetails) {
        Board board = boardService.findById(id);
        if (!board.getMember().getId().equals(userDetails.getMember().getId())) {
            return "redirect:/community";
        }

        BoardRequest boardRequest = new BoardRequest();
        boardRequest.setTitle(board.getTitle());
        boardRequest.setContent(board.getContent());
        boardRequest.setCategory(board.getCategory());

        model.addAttribute("boardRequest", boardRequest);
        model.addAttribute("boardId", id);
        model.addAttribute("categories", Category.values());
        model.addAttribute("memberName", userDetails.getMember().getMemberName());
        model.addAttribute("formAction", "/community/" + id + "/edit");

        return "community/form";
    }

    @PostMapping("/{id}/edit")
    public String updatePost(@PathVariable Long id,
                             @Valid @ModelAttribute("boardRequest") BoardRequest boardRequest,
                             BindingResult bindingResult,
                             @RequestParam("files") MultipartFile[] files,
                             Model model,
                             @AuthenticationPrincipal CustomUser userDetails) throws IOException {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", Category.values());
            model.addAttribute("memberName", userDetails.getMember().getMemberName());
            model.addAttribute("formAction", "/community/" + id + "/edit");
            model.addAttribute("boardId", id);
            return "community/form";
        }

        Board board = boardService.findById(id);
        if (!board.getMember().getId().equals(userDetails.getMember().getId())) {
            return "redirect:/community";
        }

        boolean hasAttachment = false;
        StringBuilder savedFileNames = new StringBuilder();
        StringBuilder originalFileNames = new StringBuilder();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String originalFilename = file.getOriginalFilename();
                String extension = FilenameUtils.getExtension(originalFilename);
                String baseName = FilenameUtils.getBaseName(originalFilename);

                String uuidFileName = UUID.randomUUID() + "_" + baseName;
                String fullFileName = uuidFileName + "." + extension;

                awsS3Utils.saveFile("community/", uuidFileName, file);

                savedFileNames.append(fullFileName).append(";");
                originalFileNames.append(originalFilename).append(";");
                hasAttachment = true;
            }
        }

        board.update(
                boardRequest.getTitle(),
                boardRequest.getContent(),
                boardRequest.getCategory(),
                hasAttachment ? savedFileNames.toString() : board.getAttachmentPath(),
                hasAttachment ? originalFileNames.toString() : board.getAttachmentOriginalName()
        );

        boardService.save(board);
        return "redirect:/community/" + id;
    }

    @GetMapping("/download/{filename}")
    public String redirectToS3Download(@PathVariable String filename) {
        String savedFilename = boardService.findSavedFilenameByPartialName(filename);

        if (savedFilename == null) {
            return "redirect:/community";
        }

        String presignedUrl = awsS3Utils.createPresignedGetUrl("community/", savedFilename);
        return "redirect:" + presignedUrl;
    }

    @GetMapping("/download-force/{filename}")
    public ResponseEntity<Resource> forceDownload(@PathVariable String filename) throws IOException {
        Board board = boardService.findByAttachmentFilename(filename);
        if (board == null) {
            return ResponseEntity.notFound().build();
        }

        String[] savedPaths = board.getAttachmentPath().split(";");
        String[] originalNames = board.getAttachmentOriginalName().split(";");

        String originalName = filename;
        for (int i = 0; i < savedPaths.length; i++) {
            if (savedPaths[i].equals(filename)) {
                originalName = originalNames[i];
                break;
            }
        }

        S3Resource resource = awsS3Utils.downloadFileWithFullName("community/", filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + URLEncoder.encode(originalName, StandardCharsets.UTF_8) + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                .body(resource);
    }

    @PostMapping("/image-upload")
    @ResponseBody
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile image) {
        if (image.isEmpty()) {
            return ResponseEntity.badRequest().body("No file selected");
        }

        String originalFilename = image.getOriginalFilename();
        String uuidFileName = UUID.randomUUID() + "_" + originalFilename;
        String dirName = "community/images/";

        awsS3Utils.saveFile(dirName, uuidFileName, image);
        String imageUrl = awsS3Utils.getObjectUrl(dirName, uuidFileName, image);

        return ResponseEntity.ok().body(Map.of("url", imageUrl));
    }

    @PostMapping("/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> deletePost(@PathVariable Long id,
                                        @AuthenticationPrincipal CustomUser user) {
        if (user == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        boardService.deleteByIdAndUser(id, user.getMember());
        return ResponseEntity.ok().build();
    }
}
