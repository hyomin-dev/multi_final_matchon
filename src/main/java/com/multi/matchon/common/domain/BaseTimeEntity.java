package com.multi.matchon.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public abstract class BaseTimeEntity {

    @CreationTimestamp
    @Column(name="created_date", columnDefinition = "VARCHAR(100)")
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name="modified_date", columnDefinition = "VARCHAR(100)")
    private LocalDateTime modifiedDate;


}
