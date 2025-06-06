// src/test/java/com/chandra/ecom_service/integration/CategoryProductIntegrationTest.java
package com.chandra.ecom_service.integration;

import com.chandra.ecom_service.dto.CategoryDto;
import com.chandra.ecom_service.dto.CreateCategoryRequest;
import com.chandra.ecom_service.dto.CreateProductRequest;
import com.chandra.ecom_service.dto.ProductDto;
import com.chandra.ecom_service.entity.Category;
import com.chandra.ecom_service.entity.Product;
import com.chandra.ecom_service.repository.CategoryRepository;
import com.chandra.ecom_service.repository.ProductRepository;
import com.chandra.ecom_service.service.CategoryService;
import com.chandra.ecom_service.service.ProductService;
import com.chandra.ecom_service.testutils.CategoryTestDataBuilder;
import com.chandra.ecom_service.testutils.ProductTestDataBuilder;
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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
class CategoryProductIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void shouldCreateCategoryHierarchyAndAssignProducts() throws Exception {
        // Given - Create parent category
        CreateCategoryRequest parentRequest = CategoryTestDataBuilder.createCategoryRequest();

        String parentResponse = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(parentRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        CategoryDto parentCategory = objectMapper.readValue(parentResponse, CategoryDto.class);

        // Create subcategory
        CreateCategoryRequest subRequest = CategoryTestDataBuilder.createSubCategoryRequest(parentCategory.getId());

        String subResponse = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        CategoryDto subCategory = objectMapper.readValue(subResponse, CategoryDto.class);

        // Create products for both categories
        CreateProductRequest productForParent = ProductTestDataBuilder.createProductRequest();
        productForParent.setCategoryId(parentCategory.getId());
        productForParent.setSku("PARENT_PRODUCT_001");
        productForParent.setName("Parent Category Product");

        CreateProductRequest productForSub = ProductTestDataBuilder.createProductRequest();
        productForSub.setCategoryId(subCategory.getId());
        productForSub.setSku("SUB_PRODUCT_001");
        productForSub.setName("Sub Category Product");

        // When - Create products
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productForParent)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productForSub)))
                .andExpect(status().isCreated());

        // Then - Verify hierarchy and product assignments
        mockMvc.perform(get("/api/categories/root"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Electronics")));

        mockMvc.perform(get("/api/categories/{parentId}/subcategories", parentCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Smartphones")));

        mockMvc.perform(get("/api/products/category/{categoryId}", parentCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Parent Category Product")));

        mockMvc.perform(get("/api/products/category/{categoryId}", subCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Sub Category Product")));
    }

    @Test
    void shouldHandleCategoryDeletionWithAssignedProducts() {
        // Given - Create category and products
        CategoryDto category = categoryService.createCategory(CategoryTestDataBuilder.createCategoryDto());

        ProductDto product1 = ProductTestDataBuilder.createProductDto();
        product1.setCategoryId(category.getId());
        productService.createProduct(product1);

        ProductDto product2 = ProductTestDataBuilder.createProductDto();
        product2.setSku("PRODUCT_002");
        product2.setCategoryId(category.getId());
        productService.createProduct(product2);

        // When - Delete category (soft delete)
        categoryService.deleteCategory(category.getId());

        // Then - Verify category is soft deleted but products remain
        List<CategoryDto> activeCategories = categoryService.getActiveCategories();
        assertThat(activeCategories).isEmpty();

        // Products should still exist but might need to handle orphaned category reference
        List<ProductDto> categoryProducts = productService.getProductsByCategory(category.getId());
        assertThat(categoryProducts).hasSize(2); // Products still exist

        // Verify category is marked as inactive in database
        Category deletedCategory = categoryRepository.findById(category.getId()).orElse(null);
        assertThat(deletedCategory).isNotNull();
        assertThat(deletedCategory.getIsActive()).isFalse();
    }

    @Test
    void shouldHandleProductsWithInvalidCategoryAssignment() throws Exception {
        // Given - Product with non-existent category
        CreateProductRequest request = ProductTestDataBuilder.createProductRequest();
        request.setCategoryId(999L); // Non-existent category

        // When & Then - Should still create product (business decision: allow orphaned products)
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId", is(999)));

        // Verify product exists but category doesn't
        mockMvc.perform(get("/api/products/category/{categoryId}", 999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldMaintainDataIntegrityDuringCascadingOperations() {
        // Given - Complex hierarchy with multiple products
        CategoryDto electronics = categoryService.createCategory(CategoryTestDataBuilder.createCategoryDto());

        CategoryDto smartphones = CategoryTestDataBuilder.createSubCategoryDto(electronics.getId());
        smartphones = categoryService.createCategory(smartphones);

        CategoryDto tablets = CategoryTestDataBuilder.createSubCategoryDto(electronics.getId());
        tablets.setName("Tablets");
        tablets = categoryService.createCategory(tablets);

        // Create products for each category
        ProductDto iphone = ProductTestDataBuilder.createProductDto();
        iphone.setCategoryId(smartphones.getId());
        iphone = productService.createProduct(iphone);

        ProductDto samsung = ProductTestDataBuilder.createSamsungProductDto();
        samsung.setCategoryId(smartphones.getId());
        samsung = productService.createProduct(samsung);

        ProductDto ipad = ProductTestDataBuilder.createProductDto();
        ipad.setSku("IPAD_001");
        ipad.setName("iPad Pro");
        ipad.setCategoryId(tablets.getId());
        ipad = productService.createProduct(ipad);

        // When - Delete parent category
        categoryService.deleteCategory(electronics.getId());

        // Then - Verify data integrity
        assertThat(categoryService.getActiveCategories()).hasSize(2); // Subcategories still active
        assertThat(productService.getActiveProducts()).hasSize(3); // All products still active

        // Subcategories should still be accessible
        List<CategoryDto> remainingCategories = categoryService.getActiveCategories();
        assertThat(remainingCategories).extracting("name").contains("Smartphones", "Tablets");

        // Products should still be accessible by their categories
        assertThat(productService.getProductsByCategory(smartphones.getId())).hasSize(2);
        assertThat(productService.getProductsByCategory(tablets.getId())).hasSize(1);
    }

    @Test
    void shouldHandleStockManagementAcrossCategories() throws Exception {
        // Given - Products in different categories
        CategoryDto category1 = categoryService.createCategory(CategoryTestDataBuilder.createCategoryDto());
        CategoryDto category2 = CategoryTestDataBuilder.createClothingCategoryDto();
        category2 = categoryService.createCategory(category2);

        CreateProductRequest product1 = ProductTestDataBuilder.createProductRequest();
        product1.setCategoryId(category1.getId());
        product1.setStockQuantity(10);

        CreateProductRequest product2 = ProductTestDataBuilder.createProductRequest();
        product2.setSku("CLOTHING_001");
        product2.setCategoryId(category2.getId());
        product2.setStockQuantity(0); // Out of stock

        // Create products
        String response1 = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product1)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String response2 = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product2)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ProductDto createdProduct1 = objectMapper.readValue(response1, ProductDto.class);
        ProductDto createdProduct2 = objectMapper.readValue(response2, ProductDto.class);

        // When & Then - Test stock management
        mockMvc.perform(get("/api/products/in-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(createdProduct1.getId().intValue())));

        mockMvc.perform(get("/api/products/out-of-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(createdProduct2.getId().intValue())));

        // Update stock and verify
        mockMvc.perform(patch("/api/products/{id}/stock", createdProduct2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 5}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products/in-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }


    @Test
    void shouldHandleFeaturedProductsAcrossCategories() {
        // Given - Featured products in different categories
        CategoryDto electronics = categoryService.createCategory(CategoryTestDataBuilder.createCategoryDto());
        CategoryDto clothing = categoryService.createCategory(CategoryTestDataBuilder.createClothingCategoryDto());

        ProductDto featuredElectronics = ProductTestDataBuilder.createFeaturedProductDto();
        featuredElectronics.setCategoryId(electronics.getId());
        featuredElectronics.setSku("FEATURED_ELECTRONICS");
        featuredElectronics.setIsFeatured(true); // Explicitly set featured
        productService.createProduct(featuredElectronics);

        ProductDto featuredClothing = ProductTestDataBuilder.createFeaturedProductDto();
        featuredClothing.setCategoryId(clothing.getId());
        featuredClothing.setSku("FEATURED_CLOTHING");
        featuredClothing.setName("Featured T-Shirt");
        featuredClothing.setBrand("Nike");
        featuredClothing.setIsFeatured(true); // Explicitly set featured
        productService.createProduct(featuredClothing);

        ProductDto regularProduct = ProductTestDataBuilder.createProductDto();
        regularProduct.setCategoryId(electronics.getId());
        regularProduct.setSku("REGULAR_PRODUCT");
        regularProduct.setIsFeatured(false);
        productService.createProduct(regularProduct);

        // When & Then
        List<ProductDto> featuredProducts = productService.getFeaturedProducts();
        assertThat(featuredProducts).hasSize(2);
        assertThat(featuredProducts).allMatch(ProductDto::getIsFeatured);
        assertThat(featuredProducts).extracting("name").contains("MacBook Air M2", "Featured T-Shirt");
    }


    @Test
    void shouldHandleComplexProductSearchAcrossCategories() throws Exception {
        // Given - Products with various attributes across categories
        CategoryDto electronics = categoryService.createCategory(CategoryTestDataBuilder.createCategoryDto());

        CreateProductRequest appleProduct1 = ProductTestDataBuilder.createProductRequest();
        appleProduct1.setCategoryId(electronics.getId());
        appleProduct1.setPrice(new BigDecimal("999.99"));
        appleProduct1.setBrand("Apple");

        CreateProductRequest appleProduct2 = ProductTestDataBuilder.createProductRequest();
        appleProduct2.setSku("MACBOOK_001");
        appleProduct2.setName("MacBook Pro");
        appleProduct2.setCategoryId(electronics.getId());
        appleProduct2.setPrice(new BigDecimal("1999.99"));
        appleProduct2.setBrand("Apple");

        CreateProductRequest samsungProduct = ProductTestDataBuilder.createProductRequest();
        samsungProduct.setSku("SAMSUNG_001");
        samsungProduct.setName("Samsung Galaxy");
        samsungProduct.setCategoryId(electronics.getId());
        samsungProduct.setPrice(new BigDecimal("799.99"));
        samsungProduct.setBrand("Samsung");

        // Create products
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appleProduct1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appleProduct2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(samsungProduct)))
                .andExpect(status().isCreated());

        // When & Then - Test various search scenarios

        // Search by brand
        mockMvc.perform(get("/api/products/brand/{brand}", "Apple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // Search by name
        mockMvc.perform(get("/api/products/search").param("name", "MacBook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", containsString("MacBook")));

        // Search by price range - fix the expected count
        mockMvc.perform(get("/api/products/price-range")
                        .param("minPrice", "800.00")
                        .param("maxPrice", "1500.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))); // Only iPhone (999.99) falls in this range

        // Search by category
        mockMvc.perform(get("/api/products/category/{categoryId}", electronics.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void shouldMaintainConsistencyDuringConcurrentOperations() {
        // Given - Simulate concurrent operations on categories and products
        CategoryDto category = categoryService.createCategory(CategoryTestDataBuilder.createCategoryDto());

        // When - Multiple products created concurrently (simulated)
        ProductDto product1 = ProductTestDataBuilder.createProductDto();
        product1.setCategoryId(category.getId());
        product1.setSku("CONCURRENT_001");

        ProductDto product2 = ProductTestDataBuilder.createProductDto();
        product2.setCategoryId(category.getId());
        product2.setSku("CONCURRENT_002");

        ProductDto product3 = ProductTestDataBuilder.createProductDto();
        product3.setCategoryId(category.getId());
        product3.setSku("CONCURRENT_003");

        // Create products
        productService.createProduct(product1);
        productService.createProduct(product2);
        productService.createProduct(product3);

        // Then - Verify consistency
        List<ProductDto> categoryProducts = productService.getProductsByCategory(category.getId());
        assertThat(categoryProducts).hasSize(3);

        List<ProductDto> allProducts = productService.getAllProducts();
        assertThat(allProducts).hasSize(3);

        // Verify unique SKUs
        assertThat(categoryProducts).extracting("sku")
                .containsExactlyInAnyOrder("CONCURRENT_001", "CONCURRENT_002", "CONCURRENT_003");
    }
}