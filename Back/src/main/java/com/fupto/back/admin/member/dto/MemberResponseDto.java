package com.fupto.back.admin.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class MemberResponseDto {
    private Long totalCount;        // 전체 회원 수
    private Long totalPages;        // 전체 페이지 수
    private Boolean hasNextPage;    // 다음 페이지 존재 여부
    private Boolean hasPrevPage;    // 이전 페이지 존재 여부
    private List<Long> pages;       // 페이지 번호 목록
    private List<MemberListDto> members;  // 회원 목록
}
