// src/test/java/com/chandra/ecom_service/service/CategoryServiceTest.java
package com.chandra.ecom_service.service;

import com.chandra.ecom_service.dto.CategoryDto;
import com.chandra.ecom_service.entity.Category;
import com.chandra.ecom_service.repository.CategoryRepository;
import com.chandra.ecom_service.service.impl.CategoryServiceImpl;
import com.chandra.ecom_service.testutils.CategoryTestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category parentCategory;
    private Category subCategory;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        parentCategory = CategoryTestDataBuilder.createCategoryEntity();
        subCategory = CategoryTestDataBuilder.createSubCategoryEntity(1L);
        categoryDto = CategoryTestDataBuilder.createCategoryDto();
    }

    @Test
    void shouldCreateCategorySuccessfully() {
        // Given
        when(categoryRepository.existsByName(anyString())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(parentCategory);

        // When
        CategoryDto result = categoryService.createCategory(categoryDto);

        // Then
        assertThat(result.getName()).isEqualTo("Electronics");
        assertThat(result.getDescription()).isEqualTo("Electronic devices and accessories");
        verify(categoryRepository).existsByName("Electronics");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void shouldThrowExceptionWhenCategoryNameAlreadyExists() {
        // Given
        when(categoryRepository.existsByName(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(categoryDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category with name Electronics already exists");

        verify(categoryRepository).existsByName("Electronics");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void shouldGetCategoryByIdSuccessfully() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));

        // When
        CategoryDto result = categoryService.getCategoryById(1L);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Electronics");
        verify(categoryRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFoundById() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.getCategoryById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found with id: 999");
    }

    @Test
    void shouldGetCategoryByNameSuccessfully() {
        // Given
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(parentCategory));

        // When
        CategoryDto result = categoryService.getCategoryByName("Electronics");

        // Then
        assertThat(result.getName()).isEqualTo("Electronics");
        verify(categoryRepository).findByName("Electronics");
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFoundByName() {
        // Given
        when(categoryRepository.findByName("NonExistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.getCategoryByName("NonExistent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found with name: NonExistent");
    }

    @Test
    void shouldGetAllCategories() {
        // Given
        List<Category> categories = Arrays.asList(parentCategory, subCategory);
        when(categoryRepository.findAll()).thenReturn(categories);

        // When
        List<CategoryDto> result = categoryService.getAllCategories();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").contains("Electronics", "Smartphones");
        verify(categoryRepository).findAll();
    }

    @Test
    void shouldGetActiveCategories() {
        // Given
        List<Category> activeCategories = Arrays.asList(parentCategory, subCategory);
        when(categoryRepository.findByIsActiveTrue()).thenReturn(activeCategories);

        // When
        List<CategoryDto> result = categoryService.getActiveCategories();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(category -> category.getIsActive());
        verify(categoryRepository).findByIsActiveTrue();
    }

    @Test
    void shouldGetRootCategories() {
        // Given
        List<Category> rootCategories = Arrays.asList(parentCategory);
        when(categoryRepository.findByParentCategoryIdIsNullAndIsActiveTrue()).thenReturn(rootCategories);

        // When
        List<CategoryDto> result = categoryService.getRootCategories();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getParentCategoryId()).isNull();
        verify(categoryRepository).findByParentCategoryIdIsNullAndIsActiveTrue();
    }

    @Test
    void shouldGetSubCategories() {
        // Given
        List<Category> subCategories = Arrays.asList(subCategory);
        when(categoryRepository.findByParentCategoryIdAndIsActiveTrue(1L)).thenReturn(subCategories);

        // When
        List<CategoryDto> result = categoryService.getSubCategories(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getParentCategoryId()).isEqualTo(1L);
        verify(categoryRepository).findByParentCategoryIdAndIsActiveTrue(1L);
    }

    @Test
    void shouldUpdateCategorySuccessfully() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(parentCategory);

        CategoryDto updateDto = new CategoryDto();
        updateDto.setName("Updated Electronics");
        updateDto.setDescription("Updated description");

        // When
        CategoryDto result = categoryService.updateCategory(1L, updateDto);

        // Then
        assertThat(result.getName()).isEqualTo("Updated Electronics");
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void shouldDeleteCategorySoftly() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(parentCategory);

        // When
        categoryService.deleteCategory(1L);

        // Then
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(argThat(category -> !category.getIsActive()));
    }

    @Test
    void shouldCheckIfCategoryExistsByName() {
        // Given
        when(categoryRepository.existsByName("Electronics")).thenReturn(true);
        when(categoryRepository.existsByName("NonExistent")).thenReturn(false);

        // When & Then
        assertThat(categoryService.existsByName("Electronics")).isTrue();
        assertThat(categoryService.existsByName("NonExistent")).isFalse();
        verify(categoryRepository).existsByName("Electronics");
        verify(categoryRepository).existsByName("NonExistent");
    }
}