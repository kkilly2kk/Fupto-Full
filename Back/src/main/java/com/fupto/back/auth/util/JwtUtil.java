package com.fupto.back.auth.util;

import com.fupto.back.auth.entity.FuptoUserDetails;
import com.fupto.back.entity.Member;
import com.fupto.back.entity.RefreshToken;
import com.fupto.back.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.*;

@Component
public class JwtUtil {
    private final Key secretKey;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtUtil(@Value("${fupto.jwt.secret}")String secret,
                   RefreshTokenRepository refreshTokenRepository){
        byte[] keyBytes = Decoders.BASE64.decode(secret); // base64로 디코딩
        this.secretKey = Keys.hmacShaKeyFor(keyBytes); // 거기에 hmac로 암호화
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public boolean vaildateToken(String token){
        try{
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e){
            System.out.println("JWT 검증 실패: " + e.getMessage());
            return false;
        }
    }

    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()//파서 빌드 생성
                .setSigningKey(secretKey) //서명키 설정 (토큰과 같은 값)
                .build()//파서 생성
                .parseClaimsJws(token) //토큰 검증 (형식,서명,만료)**jws로 변경?
                .getBody(); //토큰에서 페이로드를 추출함
    }

    public Long extractId(String token){
        return extractAllClaims(token).get("id",Long.class);
    }
    public String extractUsername(String token){
        return extractAllClaims(token).getSubject();
    }
    public String extractEmail(String token){
        return extractAllClaims(token).get("email",String.class);
    }
    public String extractnickname(String token){
        return extractAllClaims(token).get("nickname",String.class);
    }
    public List<String> extractRoles(String token){
        List<Map<String, String>> roles = extractAllClaims(token).get("roles", List.class);

        List<String> roleNames = new ArrayList<>();
        for(Map<String, String> role : roles) {
            System.out.println(role);
            roleNames.add(role.get("authority"));
            System.out.println(role.get("authority"));
        }
        return roleNames;
    }
    public String generateToken(FuptoUserDetails userDetails){
        Map<String, Object> claims = new HashMap<>();
        claims.put("id",userDetails.getId());
        claims.put("username",userDetails.getUsername());
        claims.put("password",userDetails.getPassword());
        claims.put("email",userDetails.getEmail());
        claims.put("roles",userDetails.getAuthorities());
        
      return Jwts.builder()
              .setClaims(claims)
              .setSubject(userDetails.getUsername())
              .setIssuedAt(new Date())
              .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
              .signWith(secretKey, SignatureAlgorithm.HS512)
              .compact();
    }

    // 리프레시 토큰 생성
    public RefreshToken generateRefreshToken(FuptoUserDetails userDetails) {
        String refreshToken = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 14)) // 14일
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        // 기존 리프레시 토큰이 있다면 삭제
        refreshTokenRepository.findByMember_Id(userDetails.getId())
                .ifPresent(refreshTokenRepository::delete);

        // 새 리프레시 토큰 생성 및 저장
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .member(Member.builder().id(userDetails.getId()).build())
                .expiryDate(Instant.now().plusSeconds(60 * 60 * 24 * 14)) // 14일
                .build();

        return refreshTokenRepository.save(refreshTokenEntity);
    }

    // 리프레시 토큰 검증
    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 리프레시 토큰으로 새 액세스 토큰 발급
    public String refreshAccessToken(String refreshToken) {
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        // 리프레시 토큰 만료 확인
        if (refreshTokenEntity.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshTokenEntity);
            throw new RuntimeException("Refresh token expired");
        }

        Member member = refreshTokenEntity.getMember();

        // FuptoUserDetails 생성을 위한 권한 설정
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(member.getRole()));

        FuptoUserDetails userDetails = FuptoUserDetails.builder()
                .id(member.getId())
                .username(member.getUserId())
                .email(member.getEmail())
                .authorities(authorities)
                .build();

        // 새 액세스 토큰 생성
        return generateToken(userDetails);
    }

    // 리프레시 토큰 삭제
    public void deleteRefreshToken(Long memberId) {
        refreshTokenRepository.findByMember_Id(memberId)
                .ifPresent(refreshTokenRepository::delete);
    }
}
