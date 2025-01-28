package com.fupto.back.user.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberResponseDto {
    private Long id;
    private String username; //userId에 해당하는 격
    private String password;
    private String nickname;
    private LocalDate birthDate;
    private String email;
    private String tel;
    private String gender;
    private String provider;
    private String profileImg;
}
