package com.fupto.back.auth.service;

import com.fupto.back.auth.dto.SignUpRequestDto;
import com.fupto.back.auth.entity.FuptoUserDetails;
import com.fupto.back.auth.exception.UserAlreadyExistsException;
import com.fupto.back.entity.Member;
import com.fupto.back.repository.MemberRepository;
import com.fupto.back.repository.RefreshTokenRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class FuptoUserDetailService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private MemberRepository memberRepository;
    private RefreshTokenRepository refreshTokenRepository;

    public FuptoUserDetailService(MemberRepository memberRepository,
                                  PasswordEncoder passwordEncoder,
                                  ModelMapper modelMapper,
                                  RefreshTokenRepository refreshTokenRepository) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws BadCredentialsException {

        Member member = memberRepository.findByUserId(userId);

        if (member == null) {
            throw new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
        if (!member.getState()) {
            throw new BadCredentialsException("탈퇴한 회원입니다.");
        }
        if (!member.getActive()) {
            throw new BadCredentialsException("계정이 정지되었습니다. 관리자에게 문의해주세요.");
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();

        String role = member.getRole();
        authorities.add(new SimpleGrantedAuthority(role));
        System.out.println("토큰 발급했는 지 확인용----------------");
        return FuptoUserDetails.builder()
                .id(member.getId())
                .username(userId)
                .password(member.getPassword())
                .email(member.getEmail())
                .provider(member.getProvider())
                .authorities(authorities)
                .build();
    }

    @Transactional
    public void updateLoginDate (String userId){
        Member member = memberRepository.findByUserId(userId);
        if (member != null){
            member.setLoginDate(Instant.now().plusSeconds(32400));
            memberRepository.save(member);
        }
    }

    public FuptoUserDetails regNewUser(SignUpRequestDto requestDto) throws UserAlreadyExistsException {

        if (existsByUserId(requestDto.getUserId())) {
            throw new UserAlreadyExistsException("your Id is already exists");
        }

        if (existsByNickname(requestDto.getNickname())) {
            throw new UserAlreadyExistsException("your Nickname is already exists");
        }

        if (existsByEmail(requestDto.getEmail())) {
            throw new UserAlreadyExistsException("your Email is already exists");
        }

        System.out.println("서비스 전달 확인 :"+requestDto);

        //비밀번호 암호화
        requestDto.setPassword(passwordEncoder.encode(requestDto.getPassword()));

        System.out.println("암호화 확인 :"+requestDto.getPassword());

        //dto를 entity로 변환 후 저장
        Member member = modelMapper.map(requestDto, Member.class);
        if (StringUtils.hasText(requestDto.getBirthDate())) {
            member.setBirthDate(LocalDate.parse(requestDto.getBirthDate()));
        }

        member.setProvider("FUPTO");
        Member savemember = memberRepository.save(member);
        System.out.println("entity 확인 : "+member.getRole());;

        //userDetail로 변환
        FuptoUserDetails userDetails = modelMapper.map(savemember, FuptoUserDetails.class);
        userDetails.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority(savemember.getRole())));
        System.out.println("권한 확인:"+userDetails.getAuthorities());

        return userDetails;
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenRepository.findByToken(refreshToken)
                    .ifPresent(token -> refreshTokenRepository.delete(token));
        }
    }

    public boolean existsByUserId(String userId) {
        return memberRepository.existsByUserId(userId);
    }

    public boolean existsByNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }
}