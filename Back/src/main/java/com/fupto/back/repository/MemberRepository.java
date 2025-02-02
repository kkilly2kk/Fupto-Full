package com.fupto.back.repository;

import com.fupto.back.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByUserId(String userId);

    Optional<Member> findOptionalByUserId(String userId);

    Boolean existsByUserId(String userId);

    Boolean existsByEmail(String email);

    Boolean existsByNickname(String nickname);

    Optional<Member> findByProviderAndEmail(String provider, String email);

    @Query("SELECT m FROM Member m " +
            "WHERE (" +
            "(:status = 'all' AND m.state = true) OR " +
            "(:status = 'active' AND m.state = true AND m.active = true) OR " +
            "(:status = 'suspended' AND m.state = true AND m.active = false) OR " +
            "(:status = 'withdrawn' AND m.state = false)" +
            ") AND " +
            "(:role is null OR m.role = :role) AND " +
            "(:gender is null OR m.gender = :gender) AND " +
            "(:userId is null OR m.userId LIKE %:userId%) AND " +
            "(:nickname is null OR m.nickname LIKE %:nickname%) AND " +
            "(:email is null OR m.email LIKE %:email%) AND " +
            "(:provider is null OR m.provider = :provider) AND " +
            "(" +
            ":dateType = 'all' OR " +
            "(:dateType = 'create_date' AND (:startDate is null OR m.createDate >= :startDate) AND (:endDate is null OR m.createDate <= :endDate)) OR " +
            "(:dateType = 'update_date' AND (:startDate is null OR m.updateDate >= :startDate) AND (:endDate is null OR m.updateDate <= :endDate)) OR " +
            "(:dateType = 'login_date' AND (:startDate is null OR m.loginDate >= :startDate) AND (:endDate is null OR m.loginDate <= :endDate))" +
            ")")
    Page<Member> searchMembers(
            @Param("status") String status,
            @Param("role") String role,
            @Param("gender") String gender,
            @Param("userId") String userId,
            @Param("nickname") String nickname,
            @Param("email") String email,
            @Param("provider") String provider,
            @Param("dateType") String dateType,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    // 회원 상태 업데이트를 위한 메서드
    @Modifying
    @Query("UPDATE Member m SET m.active = :active WHERE m.id = :id")
    void updateMemberActive(@Param("id") Long id, @Param("active") Boolean active);

    // 회원 권한 업데이트를 위한 메서드
    @Modifying
    @Query("UPDATE Member m SET m.role = :role WHERE m.id = :id")
    void updateMemberRole(@Param("id") Long id, @Param("role") String role);
}
