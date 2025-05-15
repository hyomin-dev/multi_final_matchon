package com.multi.matchon.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public abstract class BaseEntity {
    @CreationTimestamp
    @Column(name="created_date")
    private LocalDateTime createdDate;

    @Column(name="created_person", columnDefinition = "VARCHAR(100)")
    private String createdPerson;

    @UpdateTimestamp
    @Column(name="modified_date")
    private LocalDateTime modifiedDate;

    @Column(name="modified_person", columnDefinition = "VARCHAR(100)")
    private String modifiedPerson;
}
