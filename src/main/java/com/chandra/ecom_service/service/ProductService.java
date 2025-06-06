// src/main/java/com/chandra/ecom_service/service/ProductService.java
package com.chandra.ecom_service.service;

import com.chandra.ecom_service.dto.ProductDto;
import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    ProductDto createProduct(ProductDto productDto);

    ProductDto getProductById(Long id);

    ProductDto getProductBySku(String sku);

    List<ProductDto> getAllProducts();

    List<ProductDto> getActiveProducts();

    List<ProductDto> getProductsByCategory(Long categoryId);

    List<ProductDto> getFeaturedProducts();

    List<ProductDto> getProductsByBrand(String brand);

    List<ProductDto> searchProductsByName(String name);

    List<ProductDto> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    List<ProductDto> getInStockProducts();

    List<ProductDto> getOutOfStockProducts();

    ProductDto updateProduct(Long id, ProductDto productDto);

    ProductDto updateStock(Long id, Integer quantity);

    void deleteProduct(Long id);

    boolean existsBySku(String sku);
}