package com.fupto.back.anonymous.comment.service;

import com.fupto.back.anonymous.comment.dto.CommentResponseDto;

import java.util.List;

public interface CommentService {
    List<CommentResponseDto> getCommentsByBoardId(Long boardId);
    Long getCommentCount(Long boardId);
    CommentResponseDto createComment(Long boardId, Long memberId, String content, Long parentId);
    CommentResponseDto updateComment(Long commentId, Long memberId, String content);
    void deleteComment(Long commentId, Long memberId);
}
