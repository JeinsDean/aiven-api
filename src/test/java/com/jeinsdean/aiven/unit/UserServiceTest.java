package com.jeinsdean.aiven.unit;

import com.jeinsdean.aiven.domain.user.dto.request.UserCreateRequest;
import com.jeinsdean.aiven.domain.user.dto.request.UserUpdateRequest;
import com.jeinsdean.aiven.domain.user.dto.response.UserResponse;
import com.jeinsdean.aiven.domain.user.entity.User;
import com.jeinsdean.aiven.domain.user.repository.UserRepository;
import com.jeinsdean.aiven.domain.user.service.UserService;
import com.jeinsdean.aiven.exception.BusinessException;
import com.jeinsdean.aiven.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 * UserService 단위 테스트
 *
 * @ExtendWith(MockitoExtension.class): Spring 컨텍스트 없이 순수 Mockito만 사용 (빠름)
 *
 * 테스트 전략:
 * - Repository, PasswordEncoder는 Mock으로 대체
 * - 순수 비즈니스 로직만 검증
 * - given / when / then (BDD 스타일)
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    // 공통 픽스처
    private User normalUser;
    private User oauthUser;
    private User deletedUser;

    @BeforeEach
    void setUp() {
        normalUser = User.builder()
                .email("normal@test.com")
                .password("encoded_password")
                .nickname("일반유저")
                .build();

        oauthUser = User.builder()
                .email("oauth@test.com")
                .nickname("소셜유저")
                .provider("GOOGLE")
                .providerId("google-uid-123")
                .build();

        deletedUser = User.builder()
                .email("deleted@test.com")
                .password("encoded_password")
                .nickname("탈퇴유저")
                .build();
        deletedUser.delete();
    }

    // ──────────────────────────────────────────────
    // createUser
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("정상적으로 회원가입이 완료된다")
        void success() {
            // given
            UserCreateRequest request = createRequest("new@test.com", "password123", "신규유저");
            given(userRepository.existsByEmail("new@test.com")).willReturn(false);
            given(passwordEncoder.encode("password123")).willReturn("encoded");
            given(userRepository.save(any(User.class))).willReturn(normalUser);

            // when
            UserResponse response = userService.createUser(request);

            // then
            assertThat(response).isNotNull();
            then(userRepository).should(times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("중복 이메일이면 DUPLICATE_EMAIL 예외가 발생한다")
        void throwsWhenDuplicateEmail() {
            // given
            UserCreateRequest request = createRequest("normal@test.com", "password123", "닉네임");
            given(userRepository.existsByEmail("normal@test.com")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.DUPLICATE_EMAIL);

            // 중복이면 save가 호출되면 안 됨
            then(userRepository).should(never()).save(any(User.class));
        }

        @Test
        @DisplayName("회원가입 시 비밀번호는 인코딩되어 저장된다")
        void passwordIsEncoded() {
            // given
            UserCreateRequest request = createRequest("new@test.com", "rawPassword", "유저");
            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(passwordEncoder.encode("rawPassword")).willReturn("$2a$10$encoded");
            given(userRepository.save(any(User.class))).willReturn(normalUser);

            // when
            userService.createUser(request);

            // then: 평문 비밀번호가 아닌 encode()된 값으로 저장되었는지 검증
            then(passwordEncoder).should(times(1)).encode("rawPassword");
        }

        // 헬퍼 메서드
        private UserCreateRequest createRequest(String email, String password, String nickname) {
            // UserCreateRequest에 @NoArgsConstructor + setter가 없으므로
            // 리플렉션 또는 테스트 전용 생성자 활용
            // 실무에서는 테스트 픽스처 팩토리 클래스로 분리 권장
            try {
                UserCreateRequest req = new UserCreateRequest();
                setField(req, "email", email);
                setField(req, "password", password);
                setField(req, "nickname", nickname);
                return req;
            } catch (Exception e) {
                throw new RuntimeException("픽스처 생성 실패", e);
            }
        }

        private void setField(Object obj, String fieldName, String value) throws Exception {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        }
    }

    // ──────────────────────────────────────────────
    // getUser
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("getUser")
    class GetUser {

        @Test
        @DisplayName("존재하는 유저 ID면 유저 정보를 반환한다")
        void success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(normalUser));

            // when
            UserResponse response = userService.getUser(1L);

            // then
            assertThat(response.getEmail()).isEqualTo("normal@test.com");
        }

        @Test
        @DisplayName("존재하지 않는 유저 ID면 USER_NOT_FOUND 예외가 발생한다")
        void throwsWhenUserNotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUser(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("탈퇴한 유저 ID면 USER_NOT_FOUND 예외가 발생한다")
        void throwsWhenUserDeleted() {
            // given: Repository가 삭제된 유저를 반환하더라도
            given(userRepository.findById(2L)).willReturn(Optional.of(deletedUser));

            // when & then: Service에서 isDeleted() 체크로 걸러야 함
            assertThatThrownBy(() -> userService.getUser(2L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

    // ──────────────────────────────────────────────
    // updateUser
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("닉네임과 프로필 이미지를 정상 수정한다")
        void success() throws Exception {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(normalUser));

            UserUpdateRequest request = new UserUpdateRequest();
            setField(request, "nickname", "새닉네임");
            setField(request, "profileImage", "https://cdn.test.com/new.jpg");

            // when
            UserResponse response = userService.updateUser(1L, request);

            // then
            assertThat(response.getNickname()).isEqualTo("새닉네임");
        }

        @Test
        @DisplayName("존재하지 않는 유저면 USER_NOT_FOUND 예외가 발생한다")
        void throwsWhenUserNotFound() throws Exception {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            UserUpdateRequest request = new UserUpdateRequest();
            setField(request, "nickname", "닉네임");

            // when & then
            assertThatThrownBy(() -> userService.updateUser(999L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        private void setField(Object obj, String fieldName, String value) throws Exception {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        }
    }

    // ──────────────────────────────────────────────
    // updatePassword
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("updatePassword")
    class UpdatePassword {

        @Test
        @DisplayName("현재 비밀번호 확인 후 정상 변경된다")
        void success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(normalUser));
            given(passwordEncoder.matches("rawCurrent", "encoded_password")).willReturn(true);
            given(passwordEncoder.encode("newPassword123")).willReturn("new_encoded");

            // when
            userService.updatePassword(1L, "rawCurrent", "newPassword123");

            // then
            then(passwordEncoder).should(times(1)).encode("newPassword123");
        }

        @Test
        @DisplayName("OAuth2 유저는 비밀번호 변경이 불가하다")
        void throwsForOAuthUser() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(oauthUser));

            // when & then
            assertThatThrownBy(() -> userService.updatePassword(2L, "any", "newPass"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
        }

        @Test
        @DisplayName("현재 비밀번호가 틀리면 예외가 발생한다")
        void throwsWhenCurrentPasswordMismatch() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(normalUser));
            given(passwordEncoder.matches("wrongPassword", "encoded_password")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.updatePassword(1L, "wrongPassword", "newPass123"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

            // 비밀번호 변경 인코딩은 호출되면 안 됨
            then(passwordEncoder).should(never()).encode("newPass123");
        }
    }

    // ──────────────────────────────────────────────
    // deleteUser
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("정상적으로 soft delete 처리된다")
        void success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(normalUser));

            // when
            userService.deleteUser(1L);

            // then: 실제 delete 쿼리가 아닌 soft delete 처리 확인
            assertThat(normalUser.isDeleted()).isTrue();
            assertThat(normalUser.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 탈퇴한 유저면 USER_ALREADY_DELETED 예외가 발생한다")
        void throwsWhenAlreadyDeleted() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(deletedUser));

            // when & then
            assertThatThrownBy(() -> userService.deleteUser(2L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_ALREADY_DELETED);
        }
    }
}