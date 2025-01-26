package com.fupto.back.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fupto.back.auth.dto.AuthResponseDto;
import com.fupto.back.auth.entity.FuptoUserDetails;
import com.fupto.back.auth.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        FuptoUserDetails userDetails = (FuptoUserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        AuthResponseDto authResponse = AuthResponseDto.builder()
                .userId(userDetails.getId())
                .token(token)
                .build();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(authResponse));
    }
}