package com.fupto.back.anonymous.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentResponseDto {
    private Long id;
    private String content;
    private Instant createDate;
    private Instant updateDate;
    private MemberDto member;
    private Long parentId;
    private List<CommentResponseDto> children;
    private Boolean active;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MemberDto {
        private Long id;
        private String nickname;
    }
}