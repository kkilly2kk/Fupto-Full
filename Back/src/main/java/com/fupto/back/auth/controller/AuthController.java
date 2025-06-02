package com.fupto.back.auth.controller;

import com.fupto.back.auth.dto.*;
import com.fupto.back.auth.entity.FuptoUserDetails;
import com.fupto.back.auth.exception.UserAlreadyExistsException;
import com.fupto.back.auth.service.FuptoUserDetailService;
import com.fupto.back.auth.util.JwtUtil;
import com.fupto.back.entity.RefreshToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("auth")
public class AuthController {

    private AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;
    private FuptoUserDetailService fuptoUserDetailService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          FuptoUserDetailService fuptoUserDetailService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.fuptoUserDetailService = fuptoUserDetailService;
    }

    @GetMapping("test")
    public String test(@AuthenticationPrincipal UserDetails details) {
        if (details != null) {
            return "username:" + details.getUsername() + ", roles:" + details.getAuthorities() + details;
        }else {return "유저를 찾지 못헀습니다.";}
    }

    @GetMapping("/check/userId/{userId}")
    public ResponseEntity<?> checkUserId(@PathVariable String userId) {
        boolean exists = fuptoUserDetailService.existsByUserId(userId);
        return ResponseEntity.ok().body(Collections.singletonMap("exists", exists));
    }

    @GetMapping("/check/nickname/{nickname}")
    public ResponseEntity<?> checkNickname(@PathVariable String nickname) {
        boolean exists = fuptoUserDetailService.existsByNickname(nickname);
        return ResponseEntity.ok().body(Collections.singletonMap("exists", exists));
    }

    @GetMapping("/check/email/{email}")
    public ResponseEntity<?> checkEmail(@PathVariable String email) {
        boolean exists = fuptoUserDetailService.existsByEmail(email);
        return ResponseEntity.ok().body(Collections.singletonMap("exists", exists));
    }

    @PostMapping("refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        try {
            // 쿠키에서 리프레시 토큰 추출
            String refreshToken = null;
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("refreshToken".equals(cookie.getName())) {
                        refreshToken = cookie.getValue();
                        break;
                    }
                }
            }

            if (refreshToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Refresh token not found");
            }

            String newAccessToken = jwtUtil.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(TokenRefreshResponse.builder()
                    .accessToken(newAccessToken)
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("signin")
    public ResponseEntity<?> signIn(@RequestBody SignInRequestDto requestDto,
                                    HttpServletResponse response) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(requestDto.getUsername(), requestDto.getPassword());

            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            FuptoUserDetails userDetails = (FuptoUserDetails) authentication.getPrincipal();

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtUtil.generateToken(userDetails);
            RefreshToken refreshToken = jwtUtil.generateRefreshToken(userDetails);

            fuptoUserDetailService.updateLoginDate(userDetails.getUsername());

            // 리프레시 토큰을 HttpOnly 쿠키에 저장
            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken.getToken());
            refreshTokenCookie.setHttpOnly(true); // JavaScript 접근 차단
            refreshTokenCookie.setSecure(true); // HTTPS에서만 전송
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(60 * 60 * 24 * 14); // 14일
            refreshTokenCookie.setAttribute("SameSite", "Strict"); // CSRF 방지

            response.addCookie(refreshTokenCookie);

            // Response Body에는 액세스 토큰만 포함
            return ResponseEntity.ok(AuthResponseDto.builder()
                    .userId(userDetails.getId())
                    .provider(userDetails.getProvider())
                    .token(accessToken)
                    // .refreshToken() // 리프레시 토큰은 쿠키에 저장되므로 body 응답엔 포함하지 않음
                    .build());

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequestDto requestDto,
                                    HttpServletResponse response) {
        try {
            FuptoUserDetails userDetails = fuptoUserDetailService.regNewUser(requestDto);
            String accessToken = jwtUtil.generateToken(userDetails);
            RefreshToken refreshToken = jwtUtil.generateRefreshToken(userDetails);

            // 리프레시 토큰을 HttpOnly 쿠키에 저장
            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken.getToken());
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(60 * 60 * 24 * 14); // 14일
            refreshTokenCookie.setAttribute("SameSite", "Strict");

            response.addCookie(refreshTokenCookie);

            return ResponseEntity.ok(AuthResponseDto.builder()
                    .userId(userDetails.getId())
                    .provider(userDetails.getProvider())
                    .token(accessToken)
                    // .refreshToken() //리프레시 토큰은 쿠키에 저장되므로 body 응답엔 포함하지 않음
                    .build());
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during signup" + e.getMessage());
        }
    }

    @PostMapping("logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 쿠키에서 리프레시 토큰 추출
            String refreshToken = null;
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("refreshToken".equals(cookie.getName())) {
                        refreshToken = cookie.getValue();
                        break;
                    }
                }
            }

            // DB에서 리프레시 토큰 삭제
            if (refreshToken != null) {
                fuptoUserDetailService.logout(refreshToken);
            }

            // 쿠키 삭제
            Cookie refreshTokenCookie = new Cookie("refreshToken", "");
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(0);
            refreshTokenCookie.setAttribute("SameSite", "Strict");

            response.addCookie(refreshTokenCookie);

            return ResponseEntity.ok().body("Logout successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Logout failed");
        }
    }

}
