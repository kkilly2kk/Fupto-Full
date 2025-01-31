package com.fupto.back.admin.member.service;

import com.fupto.back.admin.member.dto.*;
import com.fupto.back.entity.Favorite;
import com.fupto.back.entity.Member;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;


public interface MemberService {
    // 회원 목록 관련
    MemberResponseDto getMemberList(MemberSearchDto searchDto);
    MemberDetailDto getMemberById(Long id);

    // 회원 관리 기능
    void updateMemberActive(Long id, Boolean active);
    void updateMemberRole(Long id, String role);
    void deleteMember(Long id);

    // 회원 즐겨찾기 관련
    FavoriteListDto getFavorite(Favorite favorite);
    Resource getProductImage(Long id) throws IOException;
}
