package com.jeinsdean.aiven.domain.user.dto.response;

import com.jeinsdean.aiven.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String nickname;
    private String profileImage;
    private String provider;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .build();
    }
}