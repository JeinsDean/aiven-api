package com.jeinsdean.aiven.domain.user.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {

    @Size(min = 2, max = 20, message = "닉네임은 2-20자여야 합니다.")
    private String nickname;

    private String profileImage;
}