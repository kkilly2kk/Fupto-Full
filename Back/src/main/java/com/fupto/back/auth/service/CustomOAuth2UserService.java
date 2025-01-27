package com.fupto.back.auth.service;

import com.fupto.back.auth.dto.OAuth2Dto;
import com.fupto.back.auth.entity.FuptoUserDetails;
import com.fupto.back.entity.Member;
import com.fupto.back.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Dto oAuth2Dto = OAuth2Dto.of(provider, oauth2User.getAttributes());

        Member member = saveOrUpdate(oAuth2Dto);

        return FuptoUserDetails.builder()
                .id(member.getId())
                .username(member.getUserId())
                .email(member.getEmail())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(member.getRole())))
                .build();
    }

    private Member saveOrUpdate(OAuth2Dto dto) {
        Member member = memberRepository.findByProviderAndEmail(dto.getProvider(), dto.getEmail())
                .map(entity -> entity.update(dto.getName()))
                .orElseGet(() -> createMember(dto));
        member.setLoginDate(Instant.now().plusSeconds(32400));

        return memberRepository.save(member);
    }

    private Member createMember(OAuth2Dto dto) {
        String randomPassword = UUID.randomUUID().toString();
        String userId = dto.getProvider().toLowerCase() + "_" + dto.getProviderId();

        return Member.builder()
                .email(dto.getEmail())
                .nickname(dto.getName())
                .userId(userId)
                .password(passwordEncoder.encode(randomPassword))
                .provider(dto.getProvider())
                .role("ROLE_USER")
                .build();
    }
}
