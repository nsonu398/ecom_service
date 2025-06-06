// src/test/java/com/chandra/ecom_service/testutils/ProductTestDataBuilder.java
package com.chandra.ecom_service.testutils;

import com.chandra.ecom_service.dto.CreateProductRequest;
import com.chandra.ecom_service.dto.ProductDto;
import com.chandra.ecom_service.entity.Product;

import java.math.BigDecimal;

public class ProductTestDataBuilder {

    public static ProductDto createProductDto() {
        ProductDto dto = new ProductDto();
        dto.setId(1L); // Set ID for test
        dto.setName("iPhone 15 Pro");
        dto.setDescription("Latest iPhone with advanced features");
        dto.setPrice(new BigDecimal("999.99"));
        dto.setSku("IPHONE15PRO001");
        dto.setCategoryId(1L);
        dto.setBrand("Apple");
        dto.setStockQuantity(50);
        dto.setWeight(new BigDecimal("0.2"));
        dto.setDimensions("15.0x7.5x0.8 cm");
        dto.setColor("Black");
        dto.setSize("6.1 inch");
        dto.setIsActive(true);
        dto.setIsFeatured(false);
        return dto;
    }

    public static ProductDto createFeaturedProductDto() {
        ProductDto dto = createProductDto();
        dto.setId(2L); // Different ID
        dto.setName("MacBook Air M2");
        dto.setSku("MACBOOKAIRM2001");
        dto.setPrice(new BigDecimal("1299.99"));
        dto.setIsFeatured(true);
        dto.setSize("13 inch");
        return dto;
    }

    public static ProductDto createOutOfStockProductDto() {
        ProductDto dto = createProductDto();
        dto.setId(3L); // Different ID
        dto.setName("iPad Pro");
        dto.setSku("IPADPRO001");
        dto.setStockQuantity(0);
        return dto;
    }

    public static CreateProductRequest createProductRequest() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("iPhone 15 Pro");
        request.setDescription("Latest iPhone with advanced features");
        request.setPrice(new BigDecimal("999.99"));
        request.setSku("IPHONE15PRO001");
        request.setCategoryId(1L);
        request.setBrand("Apple");
        request.setStockQuantity(50);
        request.setWeight(new BigDecimal("0.2"));
        request.setDimensions("15.0x7.5x0.8 cm");
        request.setColor("Black");
        request.setSize("6.1 inch");
        return request;
    }

    public static CreateProductRequest createProductRequestWithInvalidCategory() {
        CreateProductRequest request = createProductRequest();
        request.setCategoryId(999L); // Non-existent category
        return request;
    }

    public static Product createProductEntity() {
        Product product = new Product();
        product.setId(1L); // Set ID for test
        product.setName("iPhone 15 Pro");
        product.setDescription("Latest iPhone with advanced features");
        product.setPrice(new BigDecimal("999.99"));
        product.setSku("IPHONE15PRO001");
        product.setCategoryId(1L);
        product.setBrand("Apple");
        product.setStockQuantity(50);
        product.setWeight(new BigDecimal("0.2"));
        product.setDimensions("15.0x7.5x0.8 cm");
        product.setColor("Black");
        product.setSize("6.1 inch");
        product.setIsActive(true);
        product.setIsFeatured(false);
        return product;
    }

    public static Product createProductEntityWithCategory(Long categoryId) {
        Product product = createProductEntity();
        product.setCategoryId(categoryId);
        return product;
    }

    public static ProductDto createSamsungProductDto() {
        ProductDto dto = new ProductDto();
        dto.setId(4L); // Different ID
        dto.setName("Samsung Galaxy S24");
        dto.setDescription("Android smartphone with AI features");
        dto.setPrice(new BigDecimal("899.99"));
        dto.setSku("GALAXYS24001");
        dto.setCategoryId(2L);
        dto.setBrand("Samsung");
        dto.setStockQuantity(30);
        dto.setColor("Gray");
        dto.setIsActive(true);
        dto.setIsFeatured(false);
        return dto;
    }
}