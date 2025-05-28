package com.multi.matchon.stadium.domain;

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
@Table(name="stadium")
//@Setter: JPA entity에서 setter사용은 자제, test용
public class Stadium extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="stadium_id")
    private Long id;

    // 시설 이름
    @Column(name="stadium_name", nullable = false, columnDefinition = "VARCHAR(100)")
    private String faciNm;

    // 시도명
    @Column(name="stadium_region", nullable = false, columnDefinition = "VARCHAR(100)")
    private String cpNm;

    // 도로명 주소
    @Column(name="stadium_address", nullable = false, columnDefinition = "VARCHAR(255)")
    private String faciRoadAddr;

    // 전화번호
    @Column(name="stadium_Tel", nullable = false, columnDefinition = "VARCHAR(255)")
    private String faciTelNo;

    // 삭제 여부
    @Column(name="is_deleted")
    @Builder.Default
    private Boolean isDeleted=false;


}
