package com.multi.matchon.stadium.domain;

import com.multi.matchon.common.domain.BaseEntity;
import com.sun.jdi.BooleanValue;
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

    @Column(name="stadium_name", nullable = false, columnDefinition = "VARCHAR(100)")
    private String stadiumName;

    @Column(name="stadium_region", nullable = false, columnDefinition = "VARCHAR(100)")
    private String stadiumRegion;

    @Column(name="stadium_address", nullable = false, columnDefinition = "VARCHAR(255)")
    private String stadiumAddress;

    @Column(name="stadium_image_attchment_enabled", nullable = false)
    @Builder.Default
    private Boolean stadiumImageAttchmentEnabled=false;

    @Column(name="is_deleted")
    @Builder.Default
    private Boolean isDeleted=false;


}
