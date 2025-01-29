package com.fupto.back.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuptoUserDetails implements UserDetails, OAuth2User {

    private Long id;
    private String username; //userId에 해당하는 격
    private String password;
    private String email;
    private String provider;
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes; //OAuth2User의 attributes

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public String getName() {
        return this.username;
    }
}
