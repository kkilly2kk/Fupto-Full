package com.fupto.back.admin.category.service;

import com.fupto.back.admin.category.dto.CategoryCreateDto;
import com.fupto.back.admin.category.dto.CategoryListDto;
import com.fupto.back.admin.category.dto.CategoryResponseDto;
import com.fupto.back.admin.category.dto.CategorySearchDto;
import com.fupto.back.entity.Category;
import com.fupto.back.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
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
    public CategoryListDto show(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        return convertToCategoryListDto(category);
    }

    @Override
    public List<CategoryListDto> getParentCategories(Integer level) {
        return categoryRepository.findByLevel(level)
                .stream()
                .map(this::convertToCategoryListDto)
                .toList();
    }

    @Override
    public List<CategoryListDto> getChildCategories(Long parentId) {
        return categoryRepository.findByParentIdOrderByName(parentId)
                .stream()
                .map(this::convertToCategoryListDto)
                .toList();
    }

    @Override
    public CategoryListDto createCategory(CategoryCreateDto createDto) {
        Category category = new Category();
        category.setName(createDto.getName());
        category.setLevel(createDto.getLevel());

        // parent 설정
        if (createDto.getParentId() != null) {
            Category parent = categoryRepository.findById(createDto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found"));
            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        CategoryListDto result = convertToCategoryListDto(savedCategory);

        return result;
    }

    @Override
    public CategoryListDto updateCategory(Long id, CategoryCreateDto updateDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        // 기본 정보 업데이트
        category.setName(updateDto.getName());
        category.setLevel(updateDto.getLevel());
        category.setUpdateDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")).toInstant(ZoneOffset.UTC));

        // parent 설정
        if (updateDto.getParentId() != null) {
            Category parent = categoryRepository.findById(updateDto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found"));
            category.setParent(parent);
        } else {
            category.setParent(null);  // parent가 없는 경우 null로 설정
        }

        Category updatedCategory = categoryRepository.save(category);
        return convertToCategoryListDto(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    private CategoryListDto convertToCategoryListDto(Category category) {
        CategoryListDto dto = modelMapper.map(category, CategoryListDto.class);

        // 상위 카테고리 계층 구조 설정
        if (category.getParent() != null) {
            StringBuilder hierarchyBuilder = new StringBuilder();
            Category current = category.getParent();
            List<String> hierarchy = new ArrayList<>();

            // 상위 카테고리들을 리스트에 추가
            while (current != null) {
                hierarchy.add(current.getName());
                current = current.getParent();
            }

            // 역순으로 순회하며 계층 구조 문자열 생성 (1차 > 2차)
            for (int i = hierarchy.size() - 1; i >= 0; i--) {
                hierarchyBuilder.append(hierarchy.get(i));
                if (i > 0) {
                    hierarchyBuilder.append(" > ");
                }
            }

            dto.setParentId(category.getParent().getId());
            dto.setParentName(hierarchyBuilder.toString());
        }
        return dto;
    }
}
