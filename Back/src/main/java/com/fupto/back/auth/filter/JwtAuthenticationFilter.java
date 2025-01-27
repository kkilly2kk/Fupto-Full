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
import org.springframework.security.core.userdetails.UserDetails;
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


        boolean isProductsPath = request.getServletPath().startsWith("/products") &&
                !request.getServletPath().contains("/favorite");

        boolean isProtectedPath = request.getServletPath().startsWith("/myPage") ||
                request.getServletPath().startsWith("/user/member");

        boolean isAuthenticated = SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated();

        // 인증되지 않은 사용자가 접근 가능한 경로
        boolean isPublicPath = request.getServletPath().startsWith("/brands") ||
                request.getServletPath().startsWith("/shoppingmalls");

        if (!isAuthenticated && isPublicPath) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getServletPath().startsWith("/auth/") ||
                request.getServletPath().matches(".*/products/.*/image/.*") ||
                request.getServletPath().matches("/products/.*/image/.*") ||
                request.getServletPath().startsWith("/uploads/")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String token2 = request.getParameter("token");
        String token = null;

        System.out.println("authHeader : " + authHeader);
        System.out.println("request : " + request); // 확인됨

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (token2 != null && !token2.isEmpty()) {
            token = token2;
        }

            System.out.println("token : " + token);

            if (token != null && jwtUtil.vaildateToken(token)) {
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
//                    authorities.add(new SimpleGrantedAuthority(username));

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

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    System.out.println("dofilter---------------------" + authToken);
                }

        }  else if (!isProductsPath) {  // 상품 페이지도 아니고
            if (isProtectedPath) {     // 보호된 경로일때
                System.out.println("인증이 필요한 경로 접근");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
