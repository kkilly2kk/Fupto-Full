package com.fupto.back.admin.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MemberDetailDto {
    // 기본 회원 정보
    private Long id;
    private String nickname;
    private String userId;
    private String email;
    private String tel;
    private String gender;
    private String role;
    private LocalDate birthDate;
    private String provider;    // 소셜 로그인 제공자 (FUPTO, GOOGLE, NAVER, KAKAO)
    private String profileImg;

    // 계정 상태 정보
    private Boolean active;     // 계정 활성화 상태
    private Boolean state;      // 회원 탈퇴 상태

    // 활동 기록
    private Instant createDate; // 가입일
    private Instant updateDate; // 정보 수정일
    private Instant loginDate;  // 최근 로그인

    // 활동 통계
    private long boardCount;    // 게시글 수
    private long favoriteCount; // 찜 수

    // 연관 데이터
    private List<BoardListDto> boardList;       // 작성 게시글 목록
    private List<FavoriteListDto> favoriteList; // 찜 목록
}
