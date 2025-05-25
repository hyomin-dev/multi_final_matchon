package com.multi.matchon.community.domain;

import com.multi.matchon.common.domain.BaseEntity;
import com.multi.matchon.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(name = "board")
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long id;

    @Column(name = "title", nullable = false, columnDefinition = "VARCHAR(100)")
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "board_attachment_enabled", nullable = false)
    @Builder.Default
    private Boolean boardAttachmentEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    @Builder.Default
    private Category category = Category.FREEBOARD;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer", nullable = false)
    private Member member;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    public void setIsDeleted(boolean deleted) {
        this.isDeleted = deleted;
    }

    @Column(name = "attachment_path")
    private String attachmentPath;

    @Column(name = "attachment_original_name")
    private String attachmentOriginalName;

    public void update(String title, String content, Category category,
                       String attachmentPath, String attachmentOriginalName) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.attachmentPath = attachmentPath;
        this.attachmentOriginalName = attachmentOriginalName;
    }

}



