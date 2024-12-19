package com.fupto.back.admin.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryCreateDto {
    private String name;
    private Integer level;
    private Long parentId;
}
