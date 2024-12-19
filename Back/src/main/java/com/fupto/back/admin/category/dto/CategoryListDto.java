package com.fupto.back.admin.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryListDto {
    private Long id;
    private String name;
    private Integer level;
    private Long parentId;
    private String parentName;
    private Instant createDate;
    private Instant updateDate;
}
