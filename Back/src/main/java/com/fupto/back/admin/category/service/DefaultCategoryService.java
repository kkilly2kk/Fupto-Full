package com.fupto.back.admin.category.service;

import com.fupto.back.admin.category.dto.CategoryCreateDto;
import com.fupto.back.admin.category.dto.CategoryListDto;
import com.fupto.back.admin.category.dto.CategoryResponseDto;
import com.fupto.back.admin.category.dto.CategorySearchDto;
import com.fupto.back.entity.Category;
import com.fupto.back.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultCategoryService implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    public DefaultCategoryService(CategoryRepository categoryRepository, ModelMapper modelMapper) {
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public CategoryResponseDto getList(CategorySearchDto searchDto) {
        PageRequest pageable = PageRequest.of(
                searchDto.getPage(),
                searchDto.getSize(),
                Sort.by(searchDto.getSort()).descending()
        );

        Page<Category> categoryPage;
        if (searchDto.getName() != null ||
                searchDto.getLevel() != null ||
                searchDto.getStartDate() != null ||
                searchDto.getEndDate() != null) {

            categoryPage = categoryRepository.searchCategories(
                    searchDto.getName(),
                    searchDto.getLevel(),
                    searchDto.getStartDate(),
                    searchDto.getEndDate(),
                    searchDto.getDateType(),
                    pageable
            );
        } else {
            categoryPage = categoryRepository.findAll(pageable);
        }

        List<CategoryListDto> categoryListDtos = categoryPage
                .getContent()
                .stream()
                .map(this::convertToCategoryListDto)
                .toList();

        return CategoryResponseDto.builder()
                .categories(categoryListDtos)
                .totalElements(categoryPage.getTotalElements())
                .totalPages((long) categoryPage.getTotalPages())
                .currentPage(categoryPage.getNumber())
                .pageSize(categoryPage.getSize())
                .build();
    }

    @Override
    public List<CategoryListDto> getParentCategories(Integer level) {
        return categoryRepository.findByLevel(level)
                .stream()
                .map(this::convertToCategoryListDto)
                .toList();
    }

    @Override
    public CategoryListDto createCategory(CategoryCreateDto createDto) {
        Category category = modelMapper.map(createDto, Category.class);
        Category savedCategory = categoryRepository.save(category);
        return convertToCategoryListDto(savedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryListDto show(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        return convertToCategoryListDto(category);
    }

    private CategoryListDto convertToCategoryListDto(Category category) {
        CategoryListDto dto = modelMapper.map(category, CategoryListDto.class);
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }
        return dto;
    }
}
