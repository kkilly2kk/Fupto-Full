package com.fupto.back.repository;

import com.fupto.back.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c " +
            "WHERE c.board.id = :boardId " +
            "ORDER BY COALESCE(c.parent.id, c.id), c.createDate ASC")
    List<Comment> findByBoardIdOrderByCreateDateAsc(@Param("boardId") Long boardId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.board.id = :boardId")
    Long countByBoardId(@Param("boardId") Long boardId);

    void deleteByBoardId(Long boardId);
}