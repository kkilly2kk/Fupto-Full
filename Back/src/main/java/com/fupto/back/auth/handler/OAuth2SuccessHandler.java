package com.fupto.back.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fupto.back.auth.entity.FuptoUserDetails;
import com.fupto.back.auth.util.JwtUtil;
import com.fupto.back.entity.RefreshToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Value("${fupto.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        FuptoUserDetails userDetails = (FuptoUserDetails) authentication.getPrincipal();
        String accessToken = jwtUtil.generateToken(userDetails);
        RefreshToken refreshToken = jwtUtil.generateRefreshToken(userDetails);

//        String returnURL = request.getParameter("returnURL");
//        if (returnURL == null || returnURL.isEmpty()) {
//            returnURL = "/";
//        }

        // 리프레시 토큰을 HttpOnly 쿠키에 저장
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken.getToken());
        refreshTokenCookie.setHttpOnly(true);  // JavaScript로 접근 불가
        refreshTokenCookie.setSecure(true);    // HTTPS에서만 전송
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(60 * 60 * 24 * 14); // 14일
        refreshTokenCookie.setAttribute("SameSite", "Strict");  // CSRF 방지

        response.addCookie(refreshTokenCookie);

        // 액세스 토큰만 queryParam에 포함
        String targetUrl = UriComponentsBuilder
                .fromUriString(frontendUrl + "/oauth2/callback")
                .queryParam("token", accessToken)  // 액세스 토큰만 (1시간 만료)
                .queryParam("userId", userDetails.getId())
                .queryParam("provider", userDetails.getProvider())
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}