package com.fupto.back.admin.category.service;

import com.fupto.back.admin.category.dto.CategoryCreateDto;
import com.fupto.back.admin.category.dto.CategoryListDto;
import com.fupto.back.admin.category.dto.CategoryResponseDto;
import com.fupto.back.admin.category.dto.CategorySearchDto;

import java.util.List;

public interface CategoryService {
    CategoryResponseDto getList(CategorySearchDto categorySearchDto);
    CategoryListDto show(Long id);
    List<CategoryListDto> getParentCategories(Integer level);
    List<CategoryListDto> getChildCategories(Long parentId);
    CategoryListDto createCategory(CategoryCreateDto categoryCreateDto);
    CategoryListDto updateCategory(Long id, CategoryCreateDto updateDto);
    void deleteCategory(Long id);
}
