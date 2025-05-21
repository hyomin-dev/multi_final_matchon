package com.multi.matchon.team.domain;


import com.multi.matchon.common.domain.BaseEntity;
import com.multi.matchon.common.domain.Positions;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(name="team", uniqueConstraints = {@UniqueConstraint(name="UK_team_name",columnNames = {"team_name"})
})
public class Team extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="team_id")
    private Long id;

    @Column(name = "team_name", nullable = false, columnDefinition = "VARCHAR(200)")
    private String teamName;

    @Column(name="team_region", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private RegionType teamRegion;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="position_id",nullable = false)
//    private Positions position;

    @Column(name="team_rating_average",nullable = false)
    private Double teamRatingAverage;

    @Column(name="recruitment_status",nullable = false)
    @Builder.Default
    private Boolean recruitmentStatus = false;

    @Column(name="team_introduction",nullable = false,columnDefinition = "TEXT")
    private String teamIntroduction;

    @Column(name="team_attachment_enabled",nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE check (team_attachment_enabled=true)")
    @Builder.Default
    private Boolean teamAttachmentEnabled = true;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecruitingPosition> recruitingPositions = new ArrayList<>();

    @Column(name="is_deleted")
    @Builder.Default
    private Boolean isDeleted=false;







}
