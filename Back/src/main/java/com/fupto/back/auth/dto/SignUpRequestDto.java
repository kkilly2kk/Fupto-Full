package com.fupto.back.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SignUpRequestDto {
    private Long id;
    private String userId;
    private String nickname;
    private String password;
    private String birthDate;
    private String gender;
    private String tel;
    private String email;
    private String role = "ROLE_USER";
}
