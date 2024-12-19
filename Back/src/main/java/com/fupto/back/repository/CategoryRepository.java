package com.fupto.back.repository;

import com.fupto.back.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByLevel(Integer level);
    List<Category> findByLevelAndParentId(Integer level, Long parentId);
    List<Category> findByParentIdOrderByName(Long parentId);

    @Query("SELECT c FROM Category c WHERE " +
            "(:name IS NULL OR c.name LIKE %:name%) AND " +
            "(:level IS NULL OR c.level = :level) AND " +
            "(:startDate IS NULL OR " +
            "(:dateType = 'reg' AND c.createDate >= :startDate) OR " +
            "(:dateType = 'mod' AND c.updateDate >= :startDate)) AND " +
            "(:endDate IS NULL OR " +
            "(:dateType = 'reg' AND c.createDate <= :endDate) OR " +
            "(:dateType = 'mod' AND c.updateDate <= :endDate))")
    Page<Category> searchCategories(
            @Param("name") String name,
            @Param("level") Integer level,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("dateType") String dateType,
            Pageable pageable
    );
}