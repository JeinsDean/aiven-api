package com.jeinsdean.aiven.domain.user.repository;

import com.jeinsdean.aiven.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 사용자 저장소
 *
 * 대규모 서비스 최적화:
 * - 인덱스 활용 쿼리 (email, provider+providerId)
 * - Soft Delete 고려한 조회 (deleted = false)
 * - Fetch Join으로 N+1 문제 방지
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회 (Soft Delete 제외)
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deleted = false")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * OAuth2 제공자 정보로 조회
     */
    @Query("SELECT u FROM User u WHERE u.provider = :provider " +
            "AND u.providerId = :providerId AND u.deleted = false")
    Optional<User> findByProviderAndProviderId(
            @Param("provider") String provider,
            @Param("providerId") String providerId);

    /**
     * 사용자 설정과 함께 조회 (Fetch Join)
     * N+1 문제 방지
     */
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.setting " +
            "WHERE u.id = :id AND u.deleted = false")
    Optional<User> findByIdWithSetting(@Param("id") Long id);

    /**
     * 이메일 중복 확인
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM User u WHERE u.email = :email AND u.deleted = false")
    boolean existsByEmail(@Param("email") String email);
}