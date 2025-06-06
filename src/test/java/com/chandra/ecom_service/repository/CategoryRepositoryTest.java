// src/test/java/com/chandra/ecom_service/repository/CategoryRepositoryTest.java
package com.chandra.ecom_service.repository;

import com.chandra.ecom_service.dto.CategoryDto;
import com.chandra.ecom_service.entity.Category;
import com.chandra.ecom_service.testutils.CategoryTestDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldFindCategoryByName() {
        // Given
        Category category = CategoryTestDataBuilder.createCategoryEntity();
        entityManager.persistAndFlush(category);

        // When
        Optional<Category> found = categoryRepository.findByName("Electronics");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Electronics");
        assertThat(found.get().getDescription()).isEqualTo("Electronic devices and accessories");
    }

    @Test
    void shouldReturnEmptyWhenCategoryNotFoundByName() {
        // When
        Optional<Category> found = categoryRepository.findByName("NonExistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckIfCategoryExistsByName() {
        // Given
        Category category = CategoryTestDataBuilder.createCategoryEntity();
        entityManager.persistAndFlush(category);

        // When & Then
        assertThat(categoryRepository.existsByName("Electronics")).isTrue();
        assertThat(categoryRepository.existsByName("NonExistent")).isFalse();
    }

    @Test
    void shouldFindActiveCategories() {
        // Given
        Category activeCategory = CategoryTestDataBuilder.createCategoryEntity();
        Category inactiveCategory = CategoryTestDataBuilder.createCategoryEntity();
        inactiveCategory.setName("Inactive Category");
        inactiveCategory.setIsActive(false);

        entityManager.persistAndFlush(activeCategory);
        entityManager.persistAndFlush(inactiveCategory);

        // When
        List<Category> activeCategories = categoryRepository.findByIsActiveTrue();

        // Then
        assertThat(activeCategories).hasSize(1);
        assertThat(activeCategories.get(0).getName()).isEqualTo("Electronics");
        assertThat(activeCategories.get(0).getIsActive()).isTrue();
    }

    @Test
    void shouldFindSubCategoriesByParentId() {
        // Given
        Category parentCategory = CategoryTestDataBuilder.createCategoryEntity();
        entityManager.persistAndFlush(parentCategory);

        Category subCategory = CategoryTestDataBuilder.createSubCategoryEntity(parentCategory.getId());
        entityManager.persistAndFlush(subCategory);

        // When
        List<Category> subCategories = categoryRepository.findByParentCategoryIdAndIsActiveTrue(parentCategory.getId());

        // Then
        assertThat(subCategories).hasSize(1);
        assertThat(subCategories.get(0).getName()).isEqualTo("Smartphones");
        assertThat(subCategories.get(0).getParentCategoryId()).isEqualTo(parentCategory.getId());
    }

    @Test
    void shouldFindRootCategories() {
        // Given
        Category rootCategory1 = CategoryTestDataBuilder.createCategoryEntity();
        CategoryDto rootCategory2 = CategoryTestDataBuilder.createClothingCategoryDto();
        Category clothingCategory = new Category();
        clothingCategory.setName(rootCategory2.getName());
        clothingCategory.setDescription(rootCategory2.getDescription());
        clothingCategory.setIsActive(true);

        entityManager.persistAndFlush(rootCategory1);
        entityManager.persistAndFlush(clothingCategory);

        Category subCategory = CategoryTestDataBuilder.createSubCategoryEntity(rootCategory1.getId());
        entityManager.persistAndFlush(subCategory);

        // When
        List<Category> rootCategories = categoryRepository.findByParentCategoryIdIsNullAndIsActiveTrue();

        // Then
        assertThat(rootCategories).hasSize(2);
        assertThat(rootCategories).extracting("name").contains("Electronics", "Clothing");
    }

    @Test
    void shouldNotFindInactiveSubCategories() {
        // Given
        Category parentCategory = CategoryTestDataBuilder.createCategoryEntity();
        entityManager.persistAndFlush(parentCategory);

        Category activeSubCategory = CategoryTestDataBuilder.createSubCategoryEntity(parentCategory.getId());
        Category inactiveSubCategory = CategoryTestDataBuilder.createSubCategoryEntity(parentCategory.getId());
        inactiveSubCategory.setName("Inactive Smartphones");
        inactiveSubCategory.setIsActive(false);

        entityManager.persistAndFlush(activeSubCategory);
        entityManager.persistAndFlush(inactiveSubCategory);

        // When
        List<Category> activeSubCategories = categoryRepository.findByParentCategoryIdAndIsActiveTrue(parentCategory.getId());

        // Then
        assertThat(activeSubCategories).hasSize(1);
        assertThat(activeSubCategories.get(0).getName()).isEqualTo("Smartphones");
    }
}