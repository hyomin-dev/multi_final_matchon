package com.multi.matchon.customerservice.domain;

import com.multi.matchon.common.domain.BaseEntity;
import com.multi.matchon.common.domain.Status;
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
@Table(name="inquiry")
//@Setter: JPA entity에서 setter사용은 자제, test용
public class Inquiry extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="inquiry_id")
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="writer_id", nullable = false)
    private Member member;

    @Column(name = "inquiry_category", nullable = false)
    private CustomerServiceType inquiryCategory;

    @Column(name="inquiry_title",nullable = false, columnDefinition = "VARCHAR(100)")
    private String inquiryTitle;

    @Column(name="inquiry_content", nullable = false, columnDefinition = "TEXT")
    private String inquiryContent;

    @Column(name="inquiry_status",nullable = false)
    @Builder.Default
    private Status inquiryStatus = Status.PENDING;

    @Column(name="is_deleted")
    @Builder.Default
    private Boolean isDeleted=false;



}
