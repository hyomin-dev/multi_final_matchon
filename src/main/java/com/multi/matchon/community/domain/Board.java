package com.multi.matchon.community.domain;

import com.multi.matchon.common.domain.BaseEntity;
import com.multi.matchon.member.domain.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @Column(name = "title", nullable = false, columnDefinition = "VARCHAR(50)")
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

    public void update(String title, String content, Category category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

    public void update(@NotBlank(message = "제목은 필수입니다.") @Size(max = 50, message = "제목은 50자 이하로 입력해주세요.") String title, @NotBlank(message = "내용은 필수입니다.") String content, @NotNull(message = "카테고리를 선택해주세요.") Category category, Object o, Object o1) {
    }

    public void setBoardAttachmentEnabled(boolean b) {
    }
}
