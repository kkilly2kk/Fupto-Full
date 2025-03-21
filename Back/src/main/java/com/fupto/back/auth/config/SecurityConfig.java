package com.fupto.back.auth.config;

import com.fupto.back.auth.filter.JwtAuthenticationFilter;
import com.fupto.back.auth.handler.OAuth2FailureHandler;
import com.fupto.back.auth.handler.OAuth2SuccessHandler;
import com.fupto.back.auth.service.CustomOAuth2UserService;
import com.fupto.back.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {
    private JwtUtil jwtUtil;
    private PasswordEncoder passwordEncoder;
    private AuthenticationConfiguration authenticationConfiguration;
    private CustomOAuth2UserService customOAuth2UserService;
    private OAuth2SuccessHandler oauth2SuccessHandler;
    private OAuth2FailureHandler oauth2FailureHandler;

    @Value("${fupto.frontend-url}")
    private String frontendUrl;


    public SecurityConfig(JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder,
                          AuthenticationConfiguration authenticationConfiguration,
                          CustomOAuth2UserService customOAuth2UserService,
                          OAuth2SuccessHandler oauth2SuccessHandler,
                          OAuth2FailureHandler oauth2FailureHandler) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.authenticationConfiguration = authenticationConfiguration;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oauth2SuccessHandler = oauth2SuccessHandler;
        this.oauth2FailureHandler = oauth2FailureHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception{
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorsConfigurationSource corsConfSource) throws Exception {

        http.cors(cors-> cors.configurationSource(corsConfSource))
                .csrf(csrf->csrf.disable())
                .formLogin(form -> form.disable())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oauth2SuccessHandler)
                        .failureHandler(oauth2FailureHandler)
                )

                .authorizeHttpRequests(authorize-> authorize
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/products/**").permitAll()
                        .requestMatchers("/brands/**").permitAll()
                        .requestMatchers("/boards/**").permitAll()
                        .requestMatchers("/shoppingmalls/**").permitAll()
                        .requestMatchers("/admin/products/*/image/*").permitAll()
                        .requestMatchers("/products/*/image/*").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/user/member/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/myPage/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/comments/**").hasAnyRole("USER", "ADMIN")
//                        .requestMatchers("/member/**").hasAnyRole("USER","ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        System.out.println("------------필터확인완료-------------");

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfSource(){
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost", frontendUrl,"http://localhost:3001"));
        config.setAllowedMethods(Arrays.asList("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
//        config.setAllowedHeaders(Arrays.asList("*"));
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
//        config.setExposedHeaders(Arrays.asList("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",config);
        return source;
    };

//    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user1 = User.builder()
                .username("newlec")
                .password(passwordEncoder.encode("111"))
                .roles("MEMBER", "ADMIN")
                .build();

        UserDetails user2 = User.builder()
                .username("dragon")
                .password("{noop}111")
                .roles("MEMBER")
                .build();

        return new InMemoryUserDetailsManager(user1, user2);
    }

}
