package com.multi.matchon.common.jwt.domain;


import com.multi.matchon.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CurrentTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(name="refresh_token")
//@Setter: JPA entity에서 setter사용은 자제, test용
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="refresh_token_id")
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="member_id", nullable = false)
    private Member member;

    @Column(name="refresh_token_data",nullable = false,columnDefinition = "VARCHAR(512)")
    private String refreshTokenData;

    @Column(name="refresh_token_expired_date",nullable = false)
    private LocalDateTime refreshTokenExpiredDate;

    @Column(name="created_date", nullable = false)
    @CurrentTimestamp
    private LocalDateTime createdDate;


}
