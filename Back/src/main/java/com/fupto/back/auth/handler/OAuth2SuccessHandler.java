package com.fupto.back.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fupto.back.auth.dto.AuthResponseDto;
import com.fupto.back.auth.entity.FuptoUserDetails;
import com.fupto.back.auth.util.JwtUtil;
import com.fupto.back.entity.RefreshToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        FuptoUserDetails userDetails = (FuptoUserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        RefreshToken refreshToken = jwtUtil.generateRefreshToken(userDetails);

        String returnURL = request.getParameter("returnURL");
        if (returnURL == null || returnURL.isEmpty()) {
            returnURL = "/";
        }

        // 프론트엔드로 리다이렉트
        String targetUrl = UriComponentsBuilder
                .fromUriString("http://localhost:3000/oauth2/callback")
                .queryParam("token", token)
                .queryParam("refreshToken", refreshToken.getToken())
                .queryParam("userId", userDetails.getId())
                .queryParam("provider", userDetails.getProvider())
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}