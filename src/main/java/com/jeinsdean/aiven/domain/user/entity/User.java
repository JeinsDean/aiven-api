package com.jeinsdean.aiven.domain.user.entity;

import com.jeinsdean.aiven.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 엔티티
 * 공공데이터 공통표준용어 기준 컬럼명 적용
 *
 * 테이블명 : users
 * PK       : user_sn (사용자일련번호)
 *
 * 컬럼명 규칙 (snake_case → camelCase)
 *   user_sn          → userSn
 *   user_eml_addr    → userEmlAddr
 *   enpswd           → enpswd
 *   user_nm          → userNm
 *   img_file_path_nm → imgFilePathNm
 *   offr_se_cd       → offrSeCd
 *   offr_id          → offrId
 *   role_se_cd       → roleSeCd
 *   del_yn           → delYn
 *   del_dt           → delDt
 */
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_eml_addr", columnList = "user_eml_addr"),
                @Index(name = "idx_offr_se_cd_offr_id", columnList = "offr_se_cd, offr_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    /** 사용자일련번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_sn")
    private Long userSn;

    /** 사용자이메일주소 */
    @Column(name = "user_eml_addr", nullable = false, unique = true, length = 320)
    private String userEmlAddr;

    /** 암호화비밀번호 (소셜 로그인 시 null) */
    @Column(name = "pswd", length = 256)
    private String pswd;

    /** 사용자명 (닉네임) */
    @Column(name = "user_nm", nullable = false, length = 100)
    private String userNm;

    /** 이미지파일경로명 (프로필 이미지) */
    @Column(name = "img_file_path_nm", length = 500)
    private String imgFilePathNm;

    /** 제공자구분코드 (GOOGLE, KAKAO, NAVER 등) */
    @Column(name = "offr_se_cd", length = 20)
    private String offrSeCd;

    /** 제공자식별자 (OAuth2 제공자의 사용자 고유 ID) */
    @Column(name = "offr_id", length = 100)
    private String offrId;

    /** 역할구분코드 */
    @Enumerated(EnumType.STRING)
    @Column(name = "role_se_cd", nullable = false, length = 20)
    private UserRole roleSeCd = UserRole.USER;

    /** 삭제여부 */
    @Column(name = "del_yn", nullable = false)
    private Boolean delYn = false;

    /** 삭제일시 */
    @Column(name = "del_dt")
    private LocalDateTime delDt;

    /** 사용자 설정 (1:1) */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserSetting setting;

    @Builder
    public User(String userEmlAddr, String enpswd, String userNm,
                String imgFilePathNm, String offrSeCd, String offrId) {
        this.userEmlAddr = userEmlAddr;
        this.pswd = enpswd;
        this.userNm = userNm;
        this.imgFilePathNm = imgFilePathNm;
        this.offrSeCd = offrSeCd;
        this.offrId = offrId;
        this.roleSeCd = UserRole.USER;
        this.delYn = false;
    }

    // ── 비즈니스 메서드 ───────────────────────────────

    public void updateProfile(String userNm, String imgFilePathNm) {
        if (userNm != null) this.userNm = userNm;
        if (imgFilePathNm != null) this.imgFilePathNm = imgFilePathNm;
    }

    public void updatePassword(String encodedPassword) {
        this.pswd = encodedPassword;
    }

    public void assignSetting(UserSetting setting) {
        this.setting = setting;
        setting.assignUser(this);
    }

    /** Soft Delete */
    public void delete() {
        this.delYn = true;
        this.delDt = LocalDateTime.now();
    }

    /** 삭제 복구 */
    public void restore() {
        this.delYn = false;
        this.delDt = null;
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.delYn);
    }

    public boolean isOAuth2User() {
        return offrSeCd != null && offrId != null;
    }

    // ── 역할 코드 ─────────────────────────────────────

    public enum UserRole {
        USER,
        ADMIN
    }
}