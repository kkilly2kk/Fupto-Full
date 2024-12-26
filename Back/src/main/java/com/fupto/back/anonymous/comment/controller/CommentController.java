package com.fupto.back.anonymous.comment.controller;

import com.fupto.back.anonymous.comment.dto.CommentCreateDto;
import com.fupto.back.anonymous.comment.dto.CommentResponseDto;
import com.fupto.back.anonymous.comment.dto.CommentUpdateDto;
import com.fupto.back.anonymous.comment.service.CommentService;
import com.fupto.back.anonymous.comment.service.DefaultCommentService;
import com.fupto.back.entity.Comment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<CommentResponseDto>> getComments(@PathVariable Long boardId) {
        return ResponseEntity.ok(commentService.getCommentsByBoardId(boardId));
    }

    @GetMapping("/count/{boardId}")
    public ResponseEntity<Long> getCommentCount(@PathVariable Long boardId) {
        return ResponseEntity.ok(commentService.getCommentCount(boardId));
    }

    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(@RequestBody CommentCreateDto commentDto) {
        return ResponseEntity.ok(commentService.createComment(
                commentDto.getBoardId(),
                commentDto.getMemberId(),
                commentDto.getContent(),
                commentDto.getParentId()
        ));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentUpdateDto commentDto) {
        return ResponseEntity.ok(commentService.updateComment(
                commentId,
                commentDto.getMemberId(),
                commentDto.getContent()
        ));
    }

    @PatchMapping("/{commentId}/soft-delete")
    public ResponseEntity<Void> softDeleteComment(
            @PathVariable Long commentId,
            @RequestParam Long memberId
    ) {
        commentService.softDeleteComment(commentId, memberId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long memberId) {
        commentService.deleteComment(commentId, memberId);
        return ResponseEntity.noContent().build();
    }
}