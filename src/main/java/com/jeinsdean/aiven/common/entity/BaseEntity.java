package com.jeinsdean.aiven.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 모든 엔티티의 기본 클래스
 * 생성/수정 시간 및 작성자 자동 관리
 * Soft Delete 지원
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false, length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(length = 100)
    private String modifiedBy;

    /**
     * Soft Delete 플래그
     * 실제 데이터는 삭제하지 않고 논리적으로만 삭제 처리
     * 대규모 서비스에서 데이터 복구 및 감사(audit) 목적
     */
    @Column(nullable = false)
    private Boolean deleted = false;

    @Column
    private LocalDateTime deletedAt;

    /**
     * Soft Delete 처리
     * 실제 DELETE 쿼리 대신 deleted = true로 업데이트
     */
    public void delete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 삭제 취소 (복구)
     */
    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.deleted);
    }
}