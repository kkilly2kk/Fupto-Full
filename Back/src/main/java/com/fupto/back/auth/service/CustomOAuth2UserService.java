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
                .provider(member.getProvider())
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
        // 1. 이메일 중복 체크
        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new OAuth2AuthenticationException("이미 사용 중인 이메일입니다. 다른 방식으로 로그인해주세요.");
        }

        // 2. 닉네임 중복 체크 및 수정
        String nickname = dto.getName();
        String uniqueNickname = nickname;
        int suffix = 1;

        while (memberRepository.existsByNickname(uniqueNickname)) {
            uniqueNickname = nickname + suffix++;
        }

        String randomPassword = UUID.randomUUID().toString();
        String userId = dto.getProvider().toLowerCase() + "_" + dto.getProviderId();

        return Member.builder()
                .email(dto.getEmail())
                .nickname(uniqueNickname)
                .userId(userId)
                .password(passwordEncoder.encode(randomPassword))
                .provider(dto.getProvider())
                .role("ROLE_USER")
                .build();
    }
}
