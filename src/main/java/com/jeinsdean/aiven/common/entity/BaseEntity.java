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
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /** 생성일시 */
    @CreatedDate
    @Column(name = "crt_dt", nullable = false, updatable = false)
    private LocalDateTime crtDt;

    /** 수정일시 */
    @LastModifiedDate
    @Column(name = "chg_dt", nullable = false)
    private LocalDateTime chgDt;

    /** 생성자ID */
    @CreatedBy
    @Column(name = "crt_user_id", updatable = false, length = 50)
    private String crtUserId;

    /** 수정자ID */
    @LastModifiedBy
    @Column(name = "chg_user_id", length = 50)
    private String chgUserId;

}