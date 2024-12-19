package com.fupto.back.admin.category.controller;

import com.fupto.back.admin.category.dto.CategoryCreateDto;
import com.fupto.back.admin.category.dto.CategoryListDto;
import com.fupto.back.admin.category.dto.CategoryResponseDto;
import com.fupto.back.admin.category.dto.CategorySearchDto;
import com.fupto.back.admin.category.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping
    public ResponseEntity<CategoryListDto> createCategory(@RequestBody CategoryCreateDto categoryCreateDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(categoryCreateDto));
    }

    @GetMapping("parents/{level}")
    public ResponseEntity<List<CategoryListDto>> getParentCategories(@PathVariable Integer level) {
        return ResponseEntity.ok(categoryService.getParentCategories(level));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{id}/edit")
    public ResponseEntity<CategoryListDto> show(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.show(id));
    }
}
