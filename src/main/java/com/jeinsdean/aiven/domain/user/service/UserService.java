package com.jeinsdean.aiven.domain.user.service;

import com.jeinsdean.aiven.domain.user.dto.request.UserCreateRequest;
import com.jeinsdean.aiven.domain.user.dto.request.UserUpdateRequest;
import com.jeinsdean.aiven.domain.user.dto.response.UserResponse;
import com.jeinsdean.aiven.domain.user.entity.User;
import com.jeinsdean.aiven.domain.user.entity.UserSetting;
import com.jeinsdean.aiven.domain.user.repository.UserRepository;
import com.jeinsdean.aiven.exception.BusinessException;
import com.jeinsdean.aiven.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 비즈니스 로직
 *
 * 트랜잭션 전략:
 * - 조회: readOnly = true (성능 최적화)
 * - 수정: readOnly = false (기본값)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        // 이메일 중복 체크.
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 사용자 생성
        User user = User.builder()
                .userEmlAddr(request.getEmail())
                .enpswd(passwordEncoder.encode(request.getPassword()))
                .userNm(request.getNickname())
                .build();

        // 기본 설정 생성
        UserSetting setting = UserSetting.builder()
                .user(user)
                .build();
        user.assignSetting(setting);

        User savedUser = userRepository.save(user);
        log.info("User created: id={}, email={}", savedUser.getUserNm(), savedUser.getUserEmlAddr());

        return UserResponse.from(savedUser);
    }

    /**
     * 사용자 조회
     */
    public UserResponse getUser(Long userId) {
//        User user = findUserById(userId);
        User user = null;
        return UserResponse.from(user);
    }

    /**
     * 사용자 정보 수정
     */
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
//        User user = findUserById(userId);
        User user = null;
        user.updateProfile(request.getNickname(), request.getProfileImage());

        log.info("User updated: id={}", userId);
        return UserResponse.from(user);
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void updatePassword(Long userId, String currentPassword, String newPassword) {
//        User user = findUserById(userId);
        User user = null;

        // OAuth2 사용자는 비밀번호 변경 불가
        if (user.isOAuth2User()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
        }

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPswd())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "현재 비밀번호가 일치하지 않습니다.");
        }

        user.updatePassword(passwordEncoder.encode(newPassword));
        log.info("Password updated: userId={}", userId);
    }

    /**
     * 이메일로 사용자 조회
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * OAuth2 정보로 사용자 조회
     */
    public User findByProviderAndProviderId(String provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

}