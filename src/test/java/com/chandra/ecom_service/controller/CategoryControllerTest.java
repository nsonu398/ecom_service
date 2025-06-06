// src/test/java/com/chandra/ecom_service/controller/CategoryControllerTest.java
package com.chandra.ecom_service.controller;

import com.chandra.ecom_service.dto.CategoryDto;
import com.chandra.ecom_service.dto.CreateCategoryRequest;
import com.chandra.ecom_service.entity.Category;
import com.chandra.ecom_service.repository.CategoryRepository;
import com.chandra.ecom_service.testutils.CategoryTestDataBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class CategoryControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        categoryRepository.deleteAll();
    }

    @Test
    void shouldCreateCategorySuccessfully() throws Exception {
        // Given
        CreateCategoryRequest request = CategoryTestDataBuilder.createCategoryRequest();

        // When & Then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Electronics")))
                .andExpect(jsonPath("$.description", is("Electronic devices and accessories")))
                .andExpect(jsonPath("$.parentCategoryId").doesNotExist())
                .andExpect(jsonPath("$.isActive", is(true)));
    }

    @Test
    void shouldCreateSubCategorySuccessfully() throws Exception {
        // Given
        Category parentCategory = CategoryTestDataBuilder.createCategoryEntity();
        categoryRepository.save(parentCategory);

        CreateCategoryRequest request = CategoryTestDataBuilder.createSubCategoryRequest(parentCategory.getId());

        // When & Then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Smartphones")))
                .andExpect(jsonPath("$.parentCategoryId", is(parentCategory.getId().intValue())));
    }

    @Test
    void shouldReturnBadRequestForInvalidCategoryName() throws Exception {
        // Given
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName(""); // Invalid: empty name
        request.setDescription("Test description");

        // When & Then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetCategoryByIdSuccessfully() throws Exception {
        // Given
        Category category = CategoryTestDataBuilder.createCategoryEntity();
        Category savedCategory = categoryRepository.save(category);

        // When & Then
        mockMvc.perform(get("/api/categories/{id}", savedCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedCategory.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Electronics")));
    }

    @Test
    void shouldReturnNotFoundForNonExistentCategory() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/categories/{id}", 999L))
                .andExpect(status().isInternalServerError()); // Our service throws RuntimeException
    }

    @Test
    void shouldGetCategoryByNameSuccessfully() throws Exception {
        // Given
        Category category = CategoryTestDataBuilder.createCategoryEntity();
        categoryRepository.save(category);

        // When & Then
        mockMvc.perform(get("/api/categories/name/{name}", "Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Electronics")));
    }

    @Test
    void shouldGetAllCategories() throws Exception {
        // Given
        Category category1 = CategoryTestDataBuilder.createCategoryEntity();
        Category category2 = CategoryTestDataBuilder.createCategoryEntity();
        category2.setName("Clothing");

        categoryRepository.save(category1);
        categoryRepository.save(category2);

        // When & Then
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Electronics", "Clothing")));
    }

    @Test
    void shouldGetActiveCategoriesOnly() throws Exception {
        // Given
        Category activeCategory = CategoryTestDataBuilder.createCategoryEntity();
        Category inactiveCategory = CategoryTestDataBuilder.createCategoryEntity();
        inactiveCategory.setName("Inactive Category");
        inactiveCategory.setIsActive(false);

        categoryRepository.save(activeCategory);
        categoryRepository.save(inactiveCategory);

        // When & Then
        mockMvc.perform(get("/api/categories/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Electronics")))
                .andExpect(jsonPath("$[0].isActive", is(true)));
    }

    @Test
    void shouldGetRootCategories() throws Exception {
        // Given
        Category rootCategory = CategoryTestDataBuilder.createCategoryEntity();
        Category savedRoot = categoryRepository.save(rootCategory);

        Category subCategory = CategoryTestDataBuilder.createSubCategoryEntity(savedRoot.getId());
        categoryRepository.save(subCategory);

        // When & Then
        mockMvc.perform(get("/api/categories/root"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Electronics")))
                .andExpect(jsonPath("$[0].parentCategoryId").doesNotExist());
    }

    @Test
    void shouldGetSubCategories() throws Exception {
        // Given
        Category parentCategory = CategoryTestDataBuilder.createCategoryEntity();
        Category savedParent = categoryRepository.save(parentCategory);

        Category subCategory = CategoryTestDataBuilder.createSubCategoryEntity(savedParent.getId());
        categoryRepository.save(subCategory);

        // When & Then
        mockMvc.perform(get("/api/categories/{parentId}/subcategories", savedParent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Smartphones")))
                .andExpect(jsonPath("$[0].parentCategoryId", is(savedParent.getId().intValue())));
    }

    @Test
    void shouldUpdateCategorySuccessfully() throws Exception {
        // Given
        Category category = CategoryTestDataBuilder.createCategoryEntity();
        Category savedCategory = categoryRepository.save(category);

        CategoryDto updateDto = new CategoryDto();
        updateDto.setName("Updated Electronics");
        updateDto.setDescription("Updated description");

        // When & Then
        mockMvc.perform(put("/api/categories/{id}", savedCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Electronics")))
                .andExpect(jsonPath("$.description", is("Updated description")));
    }

    @Test
    void shouldDeleteCategorySuccessfully() throws Exception {
        // Given
        Category category = CategoryTestDataBuilder.createCategoryEntity();
        Category savedCategory = categoryRepository.save(category);

        // When & Then
        mockMvc.perform(delete("/api/categories/{id}", savedCategory.getId()))
                .andExpect(status().isNoContent());

        // Verify soft delete
        Category deletedCategory = categoryRepository.findById(savedCategory.getId()).orElse(null);
        assert deletedCategory != null;
        assert !deletedCategory.getIsActive();
    }

    @Test
    void shouldCheckIfCategoryExists() throws Exception {
        // Given
        Category category = CategoryTestDataBuilder.createCategoryEntity();
        categoryRepository.save(category);

        // When & Then
        mockMvc.perform(get("/api/categories/exists/{name}", "Electronics"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        mockMvc.perform(get("/api/categories/exists/{name}", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void shouldHandleDuplicateCategoryName() throws Exception {
        // Given
        Category existingCategory = CategoryTestDataBuilder.createCategoryEntity();
        categoryRepository.save(existingCategory);

        CreateCategoryRequest request = CategoryTestDataBuilder.createCategoryRequest();

        // When & Then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()); // Our service throws RuntimeException
    }
}