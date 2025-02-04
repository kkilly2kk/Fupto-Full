package com.fupto.back.auth.filter;

import com.fupto.back.auth.entity.FuptoUserDetails;
import com.fupto.back.auth.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

//        System.out.println("=== JWT Filter Debug ===");
//        System.out.println("Request URI: " + request.getRequestURI());
//        System.out.println("Method: " + request.getMethod());
//        System.out.println("Content-Type: " + request.getContentType());
//        System.out.println("Authorization header: " + request.getHeader("Authorization"));
//        System.out.println("ServletPath: " + request.getServletPath());

        // 완전 공개 경로 (인증 필요 없음)
        if (request.getServletPath().startsWith("/auth/") ||
                request.getServletPath().matches(".*/products/.*/image/.*") ||
                request.getServletPath().matches("/products/.*/image/.*") ||
                request.getServletPath().startsWith("/uploads/") ||
                request.getServletPath().startsWith("/brands") ||
                request.getServletPath().startsWith("/shoppingmalls")) {
            filterChain.doFilter(request, response);
            return;
        }

        //보호된 경로 (인증 필요)
        boolean isProtectedPath = request.getServletPath().startsWith("/myPage") ||
                request.getServletPath().startsWith("/user/member") ||
                request.getServletPath().contains("/favorite");

        String token = extractToken(request);

        // 보호된 경로인데 토큰이 없으면 401
        if (isProtectedPath) {
            if (token == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        // 토큰이 있는 경우 검증
        if (token != null) {
            if (jwtUtil.vaildateToken(token)) {
                processAuthentication(token, request);
                filterChain.doFilter(request, response);
                return;
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    // 토큰 추출
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token2 = request.getParameter("token");

        System.out.println("authHeader : " + authHeader);
        System.out.println("request : " + request);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // "Bearer " 만 제거
        } else if (token2 != null && !token2.isEmpty()) {
            return token2;
        }
        return null;
    }

    private void processAuthentication(String token, HttpServletRequest request) {
        Long id = jwtUtil.extractId(token);
        String username = jwtUtil.extractUsername(token);
        String email = jwtUtil.extractEmail(token);
        List<String> roles = jwtUtil.extractRoles(token);

        System.out.println("Extracted from token - ");
        System.out.println("id : " + id);
        System.out.println("username : " + username);
        System.out.println("email : " + email);
        System.out.println("roles : " + roles);

        if (username != null && !username.isEmpty()) {
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            for (String role : roles) {
                String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                authorities.add(new SimpleGrantedAuthority(roleWithPrefix));
                System.out.println("Added authority: " + roleWithPrefix);
            }

            FuptoUserDetails userDetails = FuptoUserDetails.builder()
                    .id(id)
                    .username(username)
                    .authorities(authorities)
                    .build();

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            System.out.println("dofilter---------------------" + authToken);
        }
    }
}
