package com.jeinsdean.aiven.domain.user.entity;

import com.jeinsdean.aiven.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 엔티티
 *
 * 대규모 서비스 설계:
 * - email을 Unique Index로 빠른 조회
 * - OAuth2 제공자별 식별자 관리
 * - 비밀번호는 nullable (소셜 로그인 지원)
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_oauth", columnList = "provider, providerId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 255)
    private String password;  // 소셜 로그인 시 null

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 500)
    private String profileImage;

    /**
     * OAuth2 제공자 (GOOGLE, KAKAO, NAVER 등)
     */
    @Column(length = 20)
    private String provider;

    /**
     * OAuth2 제공자의 사용자 식별자
     */
    @Column(length = 100)
    private String providerId;

    /**
     * 사용자 역할
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    /**
     * 사용자 설정 (1:1 관계)
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserSetting setting;

    @Builder
    public User(String email, String password, String nickname,
                String profileImage, String provider, String providerId) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.provider = provider;
        this.providerId = providerId;
        this.role = UserRole.USER;
    }

    /**
     * 비즈니스 메서드
     */
    public void updateProfile(String nickname, String profileImage) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void assignSetting(UserSetting setting) {
        this.setting = setting;
        setting.assignUser(this);
    }

    public boolean isOAuth2User() {
        return provider != null && providerId != null;
    }

    /**
     * 사용자 역할
     */
    public enum UserRole {
        USER,
        ADMIN
    }
}