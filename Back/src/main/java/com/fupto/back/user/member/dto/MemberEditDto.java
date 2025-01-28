package com.fupto.back.user.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MemberEditDto {
    private String password;
    private String newPassword;
    private String confirmPassword;
    private String nickname;
    private String birthDate;
    private String email;
    private String tel;
    private Instant updateDate;
    private String gender;
}
