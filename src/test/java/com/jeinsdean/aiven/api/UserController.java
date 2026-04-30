package com.jeinsdean.aiven.api;

import com.jeinsdean.aiven.common.dto.ApiResponse;
import com.jeinsdean.aiven.domain.user.dto.request.PasswordUpdateRequest;
import com.jeinsdean.aiven.domain.user.dto.request.UserCreateRequest;
import com.jeinsdean.aiven.domain.user.dto.request.UserUpdateRequest;
import com.jeinsdean.aiven.domain.user.dto.response.UserResponse;
import com.jeinsdean.aiven.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 API 컨트롤러
 *
 * 인증이 필요한 엔드포인트는 @AuthenticationPrincipal로 현재 유저 ID 추출
 * JWT 토큰 → JwtAuthenticationFilter → SecurityContext → @AuthenticationPrincipal
 */
@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원가입
     * POST /api/v1/users
     * 인증 불필요 (SecurityConfig에서 permitAll)
     */
    @Operation(summary = "회원가입")
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {

        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 내 정보 조회
     * GET /api/v1/users/me
     */
    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = extractUserId(userDetails);
        UserResponse response = userService.getUser(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 내 정보 수정
     * PUT /api/v1/users/me
     */
    @Operation(summary = "내 정보 수정")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request) {

        Long userId = extractUserId(userDetails);
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 비밀번호 변경
     * PATCH /api/v1/users/me/password
     */
    @Operation(summary = "비밀번호 변경")
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PasswordUpdateRequest request) {

        Long userId = extractUserId(userDetails);
        userService.updatePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * JWT 토큰의 subject(사용자 ID 문자열)를 Long으로 변환
     * JwtTokenProvider.createAccessToken()에서 subject = userId(String)으로 저장
     */
    private Long extractUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}