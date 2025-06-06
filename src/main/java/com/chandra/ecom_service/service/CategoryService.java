// src/main/java/com/chandra/ecom_service/service/CategoryService.java
package com.chandra.ecom_service.service;

import com.chandra.ecom_service.dto.CategoryDto;
import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(CategoryDto categoryDto);

    CategoryDto getCategoryById(Long id);

    CategoryDto getCategoryByName(String name);

    List<CategoryDto> getAllCategories();

    List<CategoryDto> getActiveCategories();

    List<CategoryDto> getRootCategories();

    List<CategoryDto> getSubCategories(Long parentCategoryId);

    CategoryDto updateCategory(Long id, CategoryDto categoryDto);

    void deleteCategory(Long id);

    boolean existsByName(String name);
}