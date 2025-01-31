package com.fupto.back.admin.member.dto;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberListDto {
    private Long id;
    private String nickname;
    private String userId;
    private String email;
    private String gender;
    private String tel;
    private String role;
    private LocalDate birthDate;
    private String provider;
    private String profileImg;

    // 계정 상태
    private Boolean active;
    private Boolean state;

    // 날짜 정보
    private Instant createDate;
    private Instant updateDate;
    private Instant loginDate;
}
