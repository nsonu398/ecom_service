// src/test/java/com/chandra/ecom_service/testutils/CategoryTestDataBuilder.java
package com.chandra.ecom_service.testutils;

import com.chandra.ecom_service.dto.CategoryDto;
import com.chandra.ecom_service.dto.CreateCategoryRequest;
import com.chandra.ecom_service.entity.Category;

public class CategoryTestDataBuilder {

    public static CategoryDto createCategoryDto() {
        CategoryDto dto = new CategoryDto();
        dto.setName("Electronics");
        dto.setDescription("Electronic devices and accessories");
        dto.setParentCategoryId(null);
        dto.setIsActive(true);
        return dto;
    }

    public static CategoryDto createSubCategoryDto(Long parentId) {
        CategoryDto dto = new CategoryDto();
        dto.setName("Smartphones");
        dto.setDescription("Mobile phones and accessories");
        dto.setParentCategoryId(parentId);
        dto.setIsActive(true);
        return dto;
    }

    public static CreateCategoryRequest createCategoryRequest() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Electronics");
        request.setDescription("Electronic devices and accessories");
        request.setParentCategoryId(null);
        return request;
    }

    public static CreateCategoryRequest createSubCategoryRequest(Long parentId) {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Smartphones");
        request.setDescription("Mobile phones and accessories");
        request.setParentCategoryId(parentId);
        return request;
    }

    public static Category createCategoryEntity() {
        Category category = new Category();
        // Don't set ID - let Hibernate generate it
        category.setName("Electronics");
        category.setDescription("Electronic devices and accessories");
        category.setParentCategoryId(null);
        category.setIsActive(true);
        return category;
    }

    public static Category createSubCategoryEntity(Long parentId) {
        Category category = new Category();
        // Don't set ID - let Hibernate generate it
        category.setName("Smartphones");
        category.setDescription("Mobile phones and accessories");
        category.setParentCategoryId(parentId);
        category.setIsActive(true);
        return category;
    }

    public static CategoryDto createClothingCategoryDto() {
        CategoryDto dto = new CategoryDto();
        dto.setName("Clothing");
        dto.setDescription("Fashion and apparel");
        dto.setParentCategoryId(null);
        dto.setIsActive(true);
        return dto;
    }
}