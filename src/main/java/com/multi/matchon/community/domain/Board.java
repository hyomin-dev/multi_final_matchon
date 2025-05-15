package com.multi.matchon.community.domain;

import com.multi.matchon.common.domain.BaseEntity;
import com.multi.matchon.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(name="board")
//@Setter: JPA entity에서 setter사용은 자제, test용
public class Board extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="board_id")
    private Long id;

    @Column(name="title", nullable = false, columnDefinition = "VARCHAR(100)")
    private String title;

    @Column(name="content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name="board_attachment_enabled", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private Boolean boardAttachmentEnabled = false;

    @Column(name="category", nullable = false)
    @Builder.Default
    private Category category = Category.FREEBOARD;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="writer", nullable = false)
    private Member member;

    @Column(name="is_deleted")
    @Builder.Default
    private Boolean isDeleted=false;


}
