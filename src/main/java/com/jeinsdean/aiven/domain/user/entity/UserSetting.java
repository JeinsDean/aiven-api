package com.jeinsdean.aiven.domain.user.entity;

import com.jeinsdean.aiven.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 맞춤 설정
 *
 * AI 제안 개인화의 핵심:
 * - 제안 빈도: 하루 몇 번 알림 받을지
 * - 성격: AI 응답 톤 (친근함, 전문적, 유머러스 등)
 * - 스타일: 응답 형식 (짧게, 길게, 이모티콘 등)
 */
@Entity
@Table(name = "user_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 제안 빈도 (하루 기준)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SuggestionFrequency suggestionFrequency = SuggestionFrequency.MODERATE;

    /**
     * AI 성격
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AIPersonality personality = AIPersonality.FRIENDLY;

    /**
     * 응답 스타일
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResponseStyle responseStyle = ResponseStyle.BALANCED;

    /**
     * 알림 활성화
     */
    @Column(nullable = false)
    private Boolean notificationEnabled = true;

    /**
     * 위치 기반 제안 활성화
     */
    @Column(nullable = false)
    private Boolean locationBasedEnabled = true;

    /**
     * 시간 기반 제안 활성화
     */
    @Column(nullable = false)
    private Boolean timeBasedEnabled = true;

    @Builder
    public UserSetting(User user, SuggestionFrequency suggestionFrequency,
                       AIPersonality personality, ResponseStyle responseStyle) {
        this.user = user;
        this.suggestionFrequency = suggestionFrequency != null ? suggestionFrequency : SuggestionFrequency.MODERATE;
        this.personality = personality != null ? personality : AIPersonality.FRIENDLY;
        this.responseStyle = responseStyle != null ? responseStyle : ResponseStyle.BALANCED;
    }

    /**
     * 비즈니스 메서드
     */
    public void assignUser(User user) {
        this.user = user;
    }

    public void updateSettings(SuggestionFrequency frequency, AIPersonality personality,
                               ResponseStyle style) {
        if (frequency != null) {
            this.suggestionFrequency = frequency;
        }
        if (personality != null) {
            this.personality = personality;
        }
        if (style != null) {
            this.responseStyle = style;
        }
    }

    public void toggleNotification() {
        this.notificationEnabled = !this.notificationEnabled;
    }

    public void toggleLocationBased() {
        this.locationBasedEnabled = !this.locationBasedEnabled;
    }

    public void toggleTimeBased() {
        this.timeBasedEnabled = !this.timeBasedEnabled;
    }

    /**
     * 제안 빈도 (하루 기준)
     */
    public enum SuggestionFrequency {
        LOW(3),        // 하루 3회
        MODERATE(5),   // 하루 5회
        HIGH(10);      // 하루 10회

        private final int dailyCount;

        SuggestionFrequency(int dailyCount) {
            this.dailyCount = dailyCount;
        }

        public int getDailyCount() {
            return dailyCount;
        }
    }

    /**
     * AI 성격
     */
    public enum AIPersonality {
        FRIENDLY,      // 친근한 친구
        PROFESSIONAL,  // 전문적인 비서
        HUMOROUS,      // 유머러스한
        MOTIVATIONAL,  // 동기부여형
        CALM           // 차분한
    }

    /**
     * 응답 스타일
     */
    public enum ResponseStyle {
        CONCISE,       // 간결하게 (1-2줄)
        BALANCED,      // 적당히 (3-5줄)
        DETAILED,      // 자세히 (5줄 이상)
        WITH_EMOJI,    // 이모티콘 포함
        VISUAL         // 그림/다이어그램 선호
    }
}