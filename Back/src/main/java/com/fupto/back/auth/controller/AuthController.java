package com.fupto.back.auth.controller;

import com.fupto.back.auth.dto.SignUpRequestDto;
import com.fupto.back.auth.dto.SignInRequestDto;
import com.fupto.back.auth.dto.AuthResponseDto;
import com.fupto.back.auth.entity.FuptoUserDetails;
import com.fupto.back.auth.exception.UserAlreadyExistsException;
import com.fupto.back.auth.service.FuptoUserDetailService;
import com.fupto.back.auth.util.JwtUtil;
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

    @PostMapping("signin")
    public ResponseEntity<?> signIn (@RequestBody SignInRequestDto requestDto){
        System.out.println(requestDto);
        String username = requestDto.getUsername();
        String password = requestDto.getPassword();

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);

        System.out.println(authenticationToken);

        try{
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            FuptoUserDetails userDetails = (FuptoUserDetails) authentication.getPrincipal();

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtUtil.generateToken(userDetails);
            fuptoUserDetailService.updateLoginDate(username);

            System.out.println("인증완료");

            return ResponseEntity.ok(AuthResponseDto
                    .builder()
                    .userId(userDetails.getId())
                    .provider(userDetails.getProvider())
                    .token(token) // 토큰 발급 (응답 본문에 담아서)
                    .build());
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("signup")
    public ResponseEntity<?> signUp (@RequestBody SignUpRequestDto requestDto){
        try {
            System.out.println("dto 매개변수 입력 :"+ requestDto);
            FuptoUserDetails userDetails = fuptoUserDetailService.regNewUser(requestDto);
            String token = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(AuthResponseDto
                    .builder()
                    .userId(userDetails.getId())
                    .token(token)
                    .build());

        } catch (UserAlreadyExistsException e) {

            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus
                    .INTERNAL_SERVER_ERROR)
                    .body("Error during signup"+e.getMessage());
        }
    }

}
