package com.fupto.back.repository;

import com.fupto.back.entity.Member;
import com.fupto.back.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByMember(Member member);
    Optional<RefreshToken> findByMember_Id(Long memberId);
    void deleteByMember(Member member);
    boolean existsByMember(Member member);
}
