package com.multi.matchon.community.domain;

import com.multi.matchon.common.domain.BaseEntity;
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
@Table(name="comment")
//@Setter: JPA entity에서 setter사용은 자제, test용
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="board_id", nullable = false)
    private Board board;

    @Column(name="content",nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name="is_deleted")
    @Builder.Default
    private Boolean isDeleted=false;
}
