package com.fupto.back.admin.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryResponseDto {
    private List<CategoryListDto> categories;
    private Long totalElements;
    private Long totalPages;
    private Integer currentPage;
    private Integer pageSize;
}