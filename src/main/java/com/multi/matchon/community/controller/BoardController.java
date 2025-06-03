package com.multi.matchon.community.controller;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.domain.Attachment;
import com.multi.matchon.common.domain.BoardType;
import com.multi.matchon.common.dto.UploadedFile;
import com.multi.matchon.common.repository.AttachmentRepository;
import com.multi.matchon.common.util.AwsS3Utils;
import com.multi.matchon.common.util.FileUploadHelper;
import com.multi.matchon.community.domain.Board;
import com.multi.matchon.community.domain.Category;
import com.multi.matchon.community.dto.req.BoardRequest;
import com.multi.matchon.community.dto.req.CommentRequest;
import com.multi.matchon.community.dto.res.BoardListResponse;
import com.multi.matchon.community.service.BoardService;
import com.multi.matchon.community.service.CommentService;
import com.multi.matchon.community.service.ReportService;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.domain.MemberRole;
import io.awspring.cloud.s3.S3Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final AttachmentRepository attachmentRepository;

    @GetMapping
    public String list(@RequestParam(defaultValue = "FREEBOARD") Category category,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Pageable pageable = PageRequest.of(page, 6, Sort.by("createdDate").descending());

        Page<BoardListResponse> boardsPage = boardService.findBoardsWithCommentCount(category, pageable);

        List<BoardListResponse> pinnedPosts = boardService.findPinnedByCategory(category).stream()
                .map(board -> new BoardListResponse(
                        board.getId(),
                        board.getTitle(),
                        board.getCategory().getDisplayName(),
                        board.getMember().getMemberName(),
                        board.getCreatedDate(),
                        commentService.getCommentsByBoard(board).size(), // 또는 commentRepository.countByBoardIdAndIsDeletedFalse()
                        board.isPinned()
                ))
                .toList();

        model.addAttribute("boardsPage", boardsPage);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", Category.values());
        model.addAttribute("pinnedPosts", pinnedPosts);
        return "community/view";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Board board = boardService.findById(id);
        List<Attachment> attachments = attachmentRepository.findAllByBoardTypeAndBoardNumber(BoardType.BOARD, board.getId());

        model.addAttribute("board", board);
        model.addAttribute("attachments", attachments);
        model.addAttribute("commentRequest", new CommentRequest());
        model.addAttribute("comments", commentService.getCommentsByBoard(board));
        return "community/detail";
    }

    @GetMapping("/new")
    public String form(Model model, @AuthenticationPrincipal CustomUser user) {
        if (user == null) return "redirect:/login";
        boolean isAdmin = user.getMember().getMemberRole() == MemberRole.ADMIN;

        model.addAttribute("boardRequest", new BoardRequest());
        model.addAttribute("categories", Category.values());
        model.addAttribute("memberName", user.getMember().getMemberName());
        model.addAttribute("formAction", "/community");
        model.addAttribute("isAdmin", isAdmin);
        return FORM_VIEW;
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("boardRequest") BoardRequest boardRequest,
                         BindingResult bindingResult,
                         @RequestParam("files") MultipartFile[] files,
                         Model model,
                         @AuthenticationPrincipal CustomUser user) {

        boolean isAdmin = user.getMember().getMemberRole() == MemberRole.ADMIN;

        if (!isAdmin && boardRequest.getCategory() == Category.ANNOUNCEMENT) {
            bindingResult.rejectValue("category", "accessDenied", "공지사항은 관리자만 작성할 수 있습니다.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", Category.values());
            model.addAttribute("memberName", user.getMember().getMemberName());
            model.addAttribute("formAction", "/community");
            model.addAttribute("isAdmin", isAdmin);
            return FORM_VIEW;
        }

        Member member = user.getMember();
        Board board = Board.builder()
                .title(boardRequest.getTitle())
                .content(boardRequest.getContent())
                .category(boardRequest.getCategory())
                .member(member)
                .boardAttachmentEnabled(false)
                .pinned(isAdmin && boardRequest.isPinned())
                .build();

        boardService.save(board);

        int fileOrder = 0;
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                UploadedFile uploaded = FileUploadHelper.uploadToS3(file, COMMUNITY_DIR, awsS3Utils);
                Attachment attachment = Attachment.builder()
                        .boardType(BoardType.BOARD)
                        .boardNumber(board.getId())
                        .fileOrder(fileOrder++)
                        .originalName(uploaded.getOriginalFileName())
                        .savedName(uploaded.getSavedFileName())
                        .savePath(COMMUNITY_DIR + uploaded.getSavedFileName())
                        .build();
                attachmentRepository.save(attachment);
                board.setBoardAttachmentEnabled(true);
            }
        }

        return "redirect:/community";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, @AuthenticationPrincipal CustomUser user) {
        Board board = boardService.findById(id);
        if (!board.getMember().getId().equals(user.getMember().getId())) {
            return "redirect:/community";
        }

        boolean isAdmin = user.getMember().getMemberRole() == MemberRole.ADMIN;

        BoardRequest request = new BoardRequest(board.getTitle(), board.getContent(), board.getCategory(), board.isPinned());

        model.addAttribute("boardRequest", request);
        model.addAttribute("categories", Category.values());
        model.addAttribute("memberName", user.getMember().getMemberName());
        model.addAttribute("formAction", "/community/" + id + "/edit");
        model.addAttribute("boardId", id);
        model.addAttribute("isAdmin", isAdmin);
        return FORM_VIEW;
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("boardRequest") BoardRequest boardRequest,
                         BindingResult bindingResult,
                         @RequestParam("files") MultipartFile[] files,
                         Model model,
                         @AuthenticationPrincipal CustomUser user) throws IOException {

        boolean isAdmin = user.getMember().getMemberRole() == MemberRole.ADMIN;

        if (!isAdmin && boardRequest.getCategory() == Category.ANNOUNCEMENT) {
            bindingResult.rejectValue("category", "accessDenied", "공지사항은 관리자만 작성할 수 있습니다.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", Category.values());
            model.addAttribute("memberName", user.getMember().getMemberName());
            model.addAttribute("formAction", "/community/" + id + "/edit");
            model.addAttribute("boardId", id);
            model.addAttribute("isAdmin", isAdmin);
            return FORM_VIEW;
        }

        Board board = boardService.findById(id);
        if (!board.getMember().getId().equals(user.getMember().getId())) {
            return "redirect:/community";
        }

        board.update(
                boardRequest.getTitle(),
                boardRequest.getContent(),
                boardRequest.getCategory()

        );

        if (isAdmin) {
            board.setPinned(boardRequest.isPinned());
        }

        board.setBoardAttachmentEnabled(false);

        int fileOrder = 0;
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                UploadedFile uploaded = FileUploadHelper.uploadToS3(file, COMMUNITY_DIR, awsS3Utils);
                Attachment attachment = Attachment.builder()
                        .boardType(BoardType.BOARD)
                        .boardNumber(board.getId())
                        .fileOrder(fileOrder++)
                        .originalName(uploaded.getOriginalFileName())
                        .savedName(uploaded.getSavedFileName())
                        .savePath(COMMUNITY_DIR + uploaded.getSavedFileName())
                        .build();
                attachmentRepository.save(attachment);
                board.setBoardAttachmentEnabled(true);
            }
        }

        boardService.save(board);
        return "redirect:/community/" + id;
    }

    @GetMapping("/download-force/{filename}")
    public ResponseEntity<Resource> forceDownload(@PathVariable String filename) throws IOException {
        Optional<Attachment> optional = attachmentRepository.findCommunityAttachmentBySavedName(filename);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();

        Attachment attachment = optional.get();
        S3Resource resource = awsS3Utils.downloadFileWithFullName(attachment.getSavePath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + URLEncoder.encode(attachment.getOriginalName(), StandardCharsets.UTF_8) + "\"")
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
