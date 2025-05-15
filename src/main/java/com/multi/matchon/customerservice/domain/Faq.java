package com.multi.matchon.customerservice.domain;

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
@Table(name="faq")
//@Setter: JPA entity에서 setter사용은 자제, test용
public class Faq extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="faq_id")
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="admin_id", nullable = false)
    private Member member;

    @Column(name="faq_category", nullable = false)
    private CustomerServiceType faqCategory;

    @Column(name="faq_title", nullable = false, columnDefinition = "VARCHAR(100)")
    private String faqTitle;

    @Column(name="faq_content",nullable = false, columnDefinition = "TEXT")
    private String faqContent;

    @Column(name="is_deleted")
    @Builder.Default
    private Boolean isDeleted=false;

}
