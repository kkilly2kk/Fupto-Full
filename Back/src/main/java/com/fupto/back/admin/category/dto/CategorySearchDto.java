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
public class CategorySearchDto {
    private Integer page = 0;
    private Integer size = 10;
    private String name;
    private Integer level;
    private String dateType;
    private Instant startDate;
    private Instant endDate;
    private String sort = "id";
}