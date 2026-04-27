package com.jeinsdean.aiven.repository;

import com.jeinsdean.aiven.config.JpaConfig;
import com.jeinsdean.aiven.domain.user.entity.User;
import com.jeinsdean.aiven.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    // 테스트에서 공통으로 쓸 유저 픽스처
    private User activeUser;
    private User deletedUser;
    private User oauthUser;

    @BeforeEach
    void setUp() {
        // 일반 이메일 가입 유저
        activeUser = userRepository.save(User.builder()
                .email("active@test.com")
                .password("encoded_password")
                .nickname("활성유저")
                .build());

        // 탈퇴 처리된 유저
        deletedUser = userRepository.save(User.builder()
                .email("deleted@test.com")
                .password("encoded_password")
                .nickname("탈퇴유저")
                .build());
        deletedUser.delete();
        userRepository.flush(); // DELETE 상태를 DB에 반영

        // OAuth2 소셜 로그인 유저
        oauthUser = userRepository.save(User.builder()
                .email("oauth@test.com")
                .nickname("소셜유저")
                .provider("GOOGLE")
                .providerId("google-uid-12345")
                .build());
    }

    // ──────────────────────────────────────────────
    // findByEmail
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("존재하는 이메일이면 유저를 반환한다")
        void success() {
            Optional<User> result = userRepository.findByEmail("active@test.com");

            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("active@test.com");
        }

        @Test
        @DisplayName("탈퇴한 유저는 조회되지 않는다")
        void excludesDeletedUser() {
            Optional<User> result = userRepository.findByEmail("deleted@test.com");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 이메일이면 empty를 반환한다")
        void returnsEmptyWhenNotFound() {
            Optional<User> result = userRepository.findByEmail("nobody@test.com");

            assertThat(result).isEmpty();
        }
    }

    // ──────────────────────────────────────────────
    // existsByEmail
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("existsByEmail")
    class ExistsByEmail {

        @Test
        @DisplayName("활성 유저 이메일이면 true를 반환한다")
        void returnsTrueForActiveEmail() {
            boolean exists = userRepository.existsByEmail("active@test.com");

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("탈퇴한 유저 이메일이면 false를 반환한다 (재가입 허용)")
        void returnsFalseForDeletedEmail() {
            boolean exists = userRepository.existsByEmail("deleted@test.com");

            // 핵심 비즈니스 로직:
            // 탈퇴 후 같은 이메일로 재가입 가능해야 하므로 false여야 함
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 이메일이면 false를 반환한다")
        void returnsFalseForNonExistentEmail() {
            boolean exists = userRepository.existsByEmail("nobody@test.com");

            assertThat(exists).isFalse();
        }
    }

    // ──────────────────────────────────────────────
    // findByProviderAndProviderId
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("findByProviderAndProviderId")
    class FindByProviderAndProviderId {

        @Test
        @DisplayName("provider와 providerId가 일치하면 유저를 반환한다")
        void success() {
            Optional<User> result = userRepository
                    .findByProviderAndProviderId("GOOGLE", "google-uid-12345");

            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("oauth@test.com");
        }

        @Test
        @DisplayName("provider가 다르면 조회되지 않는다")
        void returnsEmptyWhenProviderMismatch() {
            Optional<User> result = userRepository
                    .findByProviderAndProviderId("KAKAO", "google-uid-12345");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("providerId가 다르면 조회되지 않는다")
        void returnsEmptyWhenProviderIdMismatch() {
            Optional<User> result = userRepository
                    .findByProviderAndProviderId("GOOGLE", "wrong-uid-99999");

            assertThat(result).isEmpty();
        }
    }

    // ──────────────────────────────────────────────
    // findByIdWithSetting (Fetch Join)
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("findByIdWithSetting")
    class FindByIdWithSetting {

        @Test
        @DisplayName("유저 ID로 설정 정보를 함께 조회한다")
        void fetchesUserWithSetting() {
            Optional<User> result = userRepository.findByIdWithSetting(activeUser.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(activeUser.getId());
        }

        @Test
        @DisplayName("탈퇴한 유저는 조회되지 않는다")
        void excludesDeletedUser() {
            Optional<User> result = userRepository.findByIdWithSetting(deletedUser.getId());

            assertThat(result).isEmpty();
        }
    }

}
