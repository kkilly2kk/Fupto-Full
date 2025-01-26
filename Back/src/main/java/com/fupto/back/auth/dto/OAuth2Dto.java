package com.fupto.back.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2Dto {
    private String providerId;  // 소셜 서비스의 고유 ID
    private String provider;    // 소셜 서비스 종류 (GOOGLE, NAVER, KAKAO)
    private String email;
    private String name;

    public static OAuth2Dto of(String provider, Map<String, Object> attributes) {
        switch (provider) {
            case "google":
                return ofGoogle(provider, attributes);
            case "naver":
                return ofNaver(provider, attributes);
            case "kakao":
                return ofKakao(provider, attributes);
            default:
                throw new IllegalArgumentException("Invalid Provider Type.");
        }
    }

    private static OAuth2Dto ofGoogle(String provider, Map<String, Object> attributes) {
        return OAuth2Dto.builder()
                .provider(provider.toUpperCase())
                .providerId(String.valueOf(attributes.get("sub")))
                .email((String) attributes.get("email"))
                .name((String) attributes.get("name"))
                .build();
    }

    private static OAuth2Dto ofNaver(String provider, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        return OAuth2Dto.builder()
                .provider(provider.toUpperCase())
                .providerId((String) response.get("id"))
                .email((String) response.get("email"))
                .name((String) response.get("name"))
                .build();
    }

    private static OAuth2Dto ofKakao(String provider, Map<String, Object> attributes) {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) account.get("profile");

        return OAuth2Dto.builder()
                .provider(provider.toUpperCase())
                .providerId(String.valueOf(attributes.get("id")))
                .email((String) account.get("email"))
                .name((String) profile.get("nickname"))
                .build();
    }

}