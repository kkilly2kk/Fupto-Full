package com.fupto.back.anonymous.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentCreateDto {
    private Long boardId;
    private Long memberId;
    private String content;
    private Long parentId;
    private Boolean active = true;
}
