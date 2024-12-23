package com.fupto.back.anonymous.comment.service;

import com.fupto.back.anonymous.comment.dto.CommentResponseDto;
import com.fupto.back.entity.Board;
import com.fupto.back.entity.Comment;
import com.fupto.back.entity.Member;
import com.fupto.back.repository.BoardRepository;
import com.fupto.back.repository.CommentRepository;
import com.fupto.back.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DefaultCommentService implements CommentService {

    private CommentRepository commentRepository;
    private BoardRepository boardRepository;
    private MemberRepository memberRepository;

    public DefaultCommentService(CommentRepository commentRepository, BoardRepository boardRepository, MemberRepository memberRepository) {
        this.commentRepository = commentRepository;
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
    }

    // 게시글의 모든 댓글 조회
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByBoardId(Long boardId) {
        return commentRepository.findByBoardIdOrderByCreateDateAsc(boardId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 댓글 개수 조회
    @Transactional(readOnly = true)
    public Long getCommentCount(Long boardId) {
        return commentRepository.countByBoardId(boardId);
    }

    @Transactional
    public CommentResponseDto createComment(Long boardId, Long memberId, String content, Long parentId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .content(content)
                .board(board)
                .member(member)
                .build();

        if (parentId != null) {
            Comment parentComment = commentRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("부모 댓글을 찾을 수 없습니다."));
            comment.setParent(parentComment);
        }

        return convertToDto(commentRepository.save(comment));
    }

    @Transactional
    public CommentResponseDto updateComment(Long commentId, Long memberId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        if (!comment.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("댓글 수정 권한이 없습니다.");
        }

        comment.setContent(content);
        comment.setUpdateDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")).toInstant(ZoneOffset.UTC));

        return convertToDto(commentRepository.save(comment));
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        // 작성자 확인
        if (!comment.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("댓글 삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }

    private CommentResponseDto convertToDto(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createDate(comment.getCreateDate())
                .updateDate(comment.getUpdateDate())
                .member(CommentResponseDto.MemberDto.builder()
                        .id(comment.getMember().getId())
                        .nickname(comment.getMember().getNickname())
                        .build())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .build();
    }

}
