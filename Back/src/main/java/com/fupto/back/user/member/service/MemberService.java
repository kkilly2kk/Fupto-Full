package com.fupto.back.user.member.service;

import com.fupto.back.entity.Favorite;
import com.fupto.back.entity.Member;
import com.fupto.back.user.member.dto.*;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public interface MemberService {
    MemberResponseDto editSocialMember(Member member, MemberEditDto dto);
    MemberResponseDto editMember(Member member, MemberEditDto dto);
    Member findByUserId(String userId);
    MemberResponseDto getMember (Long id);
    Resource getProductImage(Long id) throws IOException;
    List<FavoriteListDto> getFavorites (Long id);
    List<BoardListDto> getBoards (Long memberId);
    Resource getBoardImage(Long id) throws IOException;
    void updateAlertPrice( Long favoriteId, Long memberID, Integer alertPrice);
    void updateProfileImage(Long memberId, MultipartFile file) throws IOException;
    void deleteProfileImage(Long memberId);
    void withdrawMember(Member member, MemberWithdrawalDto dto);

}
