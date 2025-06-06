package com.fupto.back.admin.category.controller;

import com.fupto.back.admin.category.dto.CategoryCreateDto;
import com.fupto.back.admin.category.dto.CategoryListDto;
import com.fupto.back.admin.category.dto.CategoryResponseDto;
import com.fupto.back.admin.category.dto.CategorySearchDto;
import com.fupto.back.admin.category.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("admin/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<CategoryResponseDto> getList(CategorySearchDto categorySearchDto) {
        return ResponseEntity.ok(categoryService.getList(categorySearchDto));
    }

    @GetMapping("parents/{level}")
    public ResponseEntity<List<CategoryListDto>> getParentCategories(@PathVariable Integer level) {
        return ResponseEntity.ok(categoryService.getParentCategories(level));
    }

    @GetMapping("children/{parentId}")
    public ResponseEntity<List<CategoryListDto>> getChildCategories(@PathVariable Long parentId) {
        return ResponseEntity.ok(categoryService.getChildCategories(parentId));
    }

    @GetMapping("{id}/edit")
    public ResponseEntity<CategoryListDto> show(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.show(id));
    }

    @PostMapping
    public ResponseEntity<CategoryListDto> createCategory(@RequestBody CategoryCreateDto categoryCreateDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(categoryCreateDto));
    }

    @PutMapping("{id}")
    public ResponseEntity<CategoryListDto> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryCreateDto categoryUpdateDto) {
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryUpdateDto));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

}
