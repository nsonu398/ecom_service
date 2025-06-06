// src/test/java/com/chandra/ecom_service/controller/ProductControllerTest.java
package com.chandra.ecom_service.controller;

import com.chandra.ecom_service.dto.CreateProductRequest;
import com.chandra.ecom_service.dto.ProductDto;
import com.chandra.ecom_service.entity.Category;
import com.chandra.ecom_service.entity.Product;
import com.chandra.ecom_service.repository.CategoryRepository;
import com.chandra.ecom_service.repository.ProductRepository;
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
import java.util.HashMap;
import java.util.Map;

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
class ProductControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        // Create test category
        testCategory = CategoryTestDataBuilder.createCategoryEntity();
        testCategory = categoryRepository.save(testCategory);
    }

    @Test
    void shouldCreateProductSuccessfully() throws Exception {
        // Given
        CreateProductRequest request = ProductTestDataBuilder.createProductRequest();
        request.setCategoryId(testCategory.getId());

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("iPhone 15 Pro")))
                .andExpect(jsonPath("$.sku", is("IPHONE15PRO001")))
                .andExpect(jsonPath("$.price", is(999.99)))
                .andExpect(jsonPath("$.categoryId", is(testCategory.getId().intValue())))
                .andExpect(jsonPath("$.brand", is("Apple")))
                .andExpect(jsonPath("$.stockQuantity", is(50)))
                .andExpect(jsonPath("$.isActive", is(true)))
                .andExpect(jsonPath("$.isFeatured", is(false)));
    }

    @Test
    void shouldReturnBadRequestForInvalidProductData() throws Exception {
        // Given
        CreateProductRequest request = new CreateProductRequest();
        request.setName(""); // Invalid: empty name
        request.setPrice(new BigDecimal("-10")); // Invalid: negative price
        request.setSku(""); // Invalid: empty SKU

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetProductByIdSuccessfully() throws Exception {
        // Given
        Product product = ProductTestDataBuilder.createProductEntity();
        product.setCategoryId(testCategory.getId());
        Product savedProduct = productRepository.save(product);

        // When & Then
        mockMvc.perform(get("/api/products/{id}", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedProduct.getId().intValue())))
                .andExpect(jsonPath("$.name", is("iPhone 15 Pro")))
                .andExpect(jsonPath("$.sku", is("IPHONE15PRO001")));
    }

    @Test
    void shouldReturnErrorForNonExistentProduct() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/products/{id}", 999L))
                .andExpect(status().isNotFound()); // Changed from isInternalServerError
    }

    @Test
    void shouldGetProductBySkuSuccessfully() throws Exception {
        // Given
        Product product = ProductTestDataBuilder.createProductEntity();
        product.setCategoryId(testCategory.getId());
        productRepository.save(product);

        // When & Then
        mockMvc.perform(get("/api/products/sku/{sku}", "IPHONE15PRO001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku", is("IPHONE15PRO001")))
                .andExpect(jsonPath("$.name", is("iPhone 15 Pro")));
    }

    @Test
    void shouldGetAllProducts() throws Exception {
        // Given
        Product product1 = ProductTestDataBuilder.createProductEntity();
        product1.setCategoryId(testCategory.getId());

        Product product2 = ProductTestDataBuilder.createProductEntity();
        product2.setSku("PRODUCT2");
        product2.setName("iPad Pro");
        product2.setCategoryId(testCategory.getId());

        productRepository.save(product1);
        productRepository.save(product2);

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("iPhone 15 Pro", "iPad Pro")));
    }

    @Test
    void shouldGetActiveProductsOnly() throws Exception {
        // Given
        Product activeProduct = ProductTestDataBuilder.createProductEntity();
        activeProduct.setCategoryId(testCategory.getId());

        Product inactiveProduct = ProductTestDataBuilder.createProductEntity();
        inactiveProduct.setSku("INACTIVE001");
        inactiveProduct.setIsActive(false);
        inactiveProduct.setCategoryId(testCategory.getId());

        productRepository.save(activeProduct);
        productRepository.save(inactiveProduct);

        // When & Then
        mockMvc.perform(get("/api/products/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sku", is("IPHONE15PRO001")))
                .andExpect(jsonPath("$[0].isActive", is(true)));
    }

    @Test
    void shouldGetProductsByCategory() throws Exception {
        // Given
        Category category2 = CategoryTestDataBuilder.createCategoryEntity();
        category2.setName("Clothing");
        category2 = categoryRepository.save(category2);

        Product product1 = ProductTestDataBuilder.createProductEntity();
        product1.setCategoryId(testCategory.getId());

        Product product2 = ProductTestDataBuilder.createProductEntity();
        product2.setSku("CLOTHING001");
        product2.setCategoryId(category2.getId());

        productRepository.save(product1);
        productRepository.save(product2);

        // When & Then
        mockMvc.perform(get("/api/products/category/{categoryId}", testCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].categoryId", is(testCategory.getId().intValue())));
    }

    @Test
    void shouldGetFeaturedProducts() throws Exception {
        // Given
        Product featuredProduct = ProductTestDataBuilder.createProductEntity();
        featuredProduct.setIsFeatured(true);
        featuredProduct.setCategoryId(testCategory.getId());

        Product regularProduct = ProductTestDataBuilder.createProductEntity();
        regularProduct.setSku("REGULAR001");
        regularProduct.setIsFeatured(false);
        regularProduct.setCategoryId(testCategory.getId());

        productRepository.save(featuredProduct);
        productRepository.save(regularProduct);

        // When & Then
        mockMvc.perform(get("/api/products/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].isFeatured", is(true)));
    }

    @Test
    void shouldGetProductsByBrand() throws Exception {
        // Given
        Product appleProduct = ProductTestDataBuilder.createProductEntity();
        appleProduct.setCategoryId(testCategory.getId());

        Product samsungProduct = ProductTestDataBuilder.createProductEntity();
        samsungProduct.setSku("SAMSUNG001");
        samsungProduct.setBrand("Samsung");
        samsungProduct.setCategoryId(testCategory.getId());

        productRepository.save(appleProduct);
        productRepository.save(samsungProduct);

        // When & Then
        mockMvc.perform(get("/api/products/brand/{brand}", "Apple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].brand", is("Apple")));
    }

    @Test
    void shouldSearchProductsByName() throws Exception {
        // Given
        Product iphone = ProductTestDataBuilder.createProductEntity();
        iphone.setCategoryId(testCategory.getId());

        Product ipad = ProductTestDataBuilder.createProductEntity();
        ipad.setSku("IPAD001");
        ipad.setName("iPad Pro");
        ipad.setCategoryId(testCategory.getId());

        productRepository.save(iphone);
        productRepository.save(ipad);

        // When & Then
        mockMvc.perform(get("/api/products/search")
                        .param("name", "iPhone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", containsString("iPhone")));
    }

    @Test
    void shouldGetProductsByPriceRange() throws Exception {
        // Given
        Product cheapProduct = ProductTestDataBuilder.createProductEntity();
        cheapProduct.setPrice(new BigDecimal("500.00"));
        cheapProduct.setSku("CHEAP001");
        cheapProduct.setCategoryId(testCategory.getId());

        Product expensiveProduct = ProductTestDataBuilder.createProductEntity();
        expensiveProduct.setPrice(new BigDecimal("1500.00"));
        expensiveProduct.setSku("EXPENSIVE001");
        expensiveProduct.setCategoryId(testCategory.getId());

        productRepository.save(cheapProduct);
        productRepository.save(expensiveProduct);

        // When & Then
        mockMvc.perform(get("/api/products/price-range")
                        .param("minPrice", "400.00")
                        .param("maxPrice", "1000.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].price", is(500.00)));
    }

    @Test
    void shouldGetInStockProducts() throws Exception {
        // Given
        Product inStockProduct = ProductTestDataBuilder.createProductEntity();
        inStockProduct.setStockQuantity(10);
        inStockProduct.setCategoryId(testCategory.getId());

        Product outOfStockProduct = ProductTestDataBuilder.createProductEntity();
        outOfStockProduct.setSku("OUTOFSTOCK001");
        outOfStockProduct.setStockQuantity(0);
        outOfStockProduct.setCategoryId(testCategory.getId());

        productRepository.save(inStockProduct);
        productRepository.save(outOfStockProduct);

        // When & Then
        mockMvc.perform(get("/api/products/in-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].stockQuantity", greaterThan(0)));
    }

    @Test
    void shouldGetOutOfStockProducts() throws Exception {
        // Given
        Product inStockProduct = ProductTestDataBuilder.createProductEntity();
        inStockProduct.setStockQuantity(10);
        inStockProduct.setCategoryId(testCategory.getId());

        Product outOfStockProduct = ProductTestDataBuilder.createProductEntity();
        outOfStockProduct.setSku("OUTOFSTOCK001");
        outOfStockProduct.setStockQuantity(0);
        outOfStockProduct.setCategoryId(testCategory.getId());

        productRepository.save(inStockProduct);
        productRepository.save(outOfStockProduct);

        // When & Then
        mockMvc.perform(get("/api/products/out-of-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].stockQuantity", is(0)));
    }

    @Test
    void shouldUpdateProductSuccessfully() throws Exception {
        // Given
        Product product = ProductTestDataBuilder.createProductEntity();
        product.setCategoryId(testCategory.getId());
        Product savedProduct = productRepository.save(product);

        ProductDto updateDto = ProductTestDataBuilder.createProductDto();
        updateDto.setName("Updated iPhone");
        updateDto.setPrice(new BigDecimal("1199.99"));
        updateDto.setIsFeatured(true);

        // When & Then
        mockMvc.perform(put("/api/products/{id}", savedProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated iPhone")))
                .andExpect(jsonPath("$.price", is(1199.99)))
                .andExpect(jsonPath("$.isFeatured", is(true)));
    }

    @Test
    void shouldUpdateStockSuccessfully() throws Exception {
        // Given
        Product product = ProductTestDataBuilder.createProductEntity();
        product.setCategoryId(testCategory.getId());
        Product savedProduct = productRepository.save(product);

        Map<String, Integer> stockUpdate = new HashMap<>();
        stockUpdate.put("quantity", 100);

        // When & Then
        mockMvc.perform(patch("/api/products/{id}/stock", savedProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity", is(100)));
    }

    @Test
    void shouldDeleteProductSuccessfully() throws Exception {
        // Given
        Product product = ProductTestDataBuilder.createProductEntity();
        product.setCategoryId(testCategory.getId());
        Product savedProduct = productRepository.save(product);

        // When & Then
        mockMvc.perform(delete("/api/products/{id}", savedProduct.getId()))
                .andExpect(status().isNoContent());

        // Verify soft delete
        Product deletedProduct = productRepository.findById(savedProduct.getId()).orElse(null);
        assert deletedProduct != null;
        assert !deletedProduct.getIsActive();
    }

    @Test
    void shouldCheckIfProductExistsBySku() throws Exception {
        // Given
        Product product = ProductTestDataBuilder.createProductEntity();
        product.setCategoryId(testCategory.getId());
        productRepository.save(product);

        // When & Then
        mockMvc.perform(get("/api/products/exists/{sku}", "IPHONE15PRO001"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        mockMvc.perform(get("/api/products/exists/{sku}", "NONEXISTENT"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void shouldHandleDuplicateSkuError() throws Exception {
        // Given
        Product existingProduct = ProductTestDataBuilder.createProductEntity();
        existingProduct.setCategoryId(testCategory.getId());
        productRepository.save(existingProduct);

        CreateProductRequest request = ProductTestDataBuilder.createProductRequest();
        request.setCategoryId(testCategory.getId());

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict()); // Changed from isInternalServerError
    }

    @Test
    void shouldHandleProductCategoryRelationship() throws Exception {
        // Given
        CreateProductRequest request = ProductTestDataBuilder.createProductRequest();
        request.setCategoryId(testCategory.getId());

        // When & Then - Create product with valid category
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId", is(testCategory.getId().intValue())));

        // Test getting products by category
        mockMvc.perform(get("/api/products/category/{categoryId}", testCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}