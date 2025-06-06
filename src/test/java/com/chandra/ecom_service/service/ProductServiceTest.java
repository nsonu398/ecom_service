// src/test/java/com/chandra/ecom_service/service/ProductServiceTest.java
package com.chandra.ecom_service.service;

import com.chandra.ecom_service.dto.ProductDto;
import com.chandra.ecom_service.entity.Product;
import com.chandra.ecom_service.repository.ProductRepository;
import com.chandra.ecom_service.service.impl.ProductServiceImpl;
import com.chandra.ecom_service.testutils.ProductTestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        product = ProductTestDataBuilder.createProductEntity();
        productDto = ProductTestDataBuilder.createProductDto();
    }

    @Test
    void shouldCreateProductSuccessfully() {
        // Given
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductDto result = productService.createProduct(productDto);

        // Then
        assertThat(result.getName()).isEqualTo("iPhone 15 Pro");
        assertThat(result.getSku()).isEqualTo("IPHONE15PRO001");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("999.99"));
        verify(productRepository).existsBySku("IPHONE15PRO001");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenSkuAlreadyExists() {
        // Given
        when(productRepository.existsBySku(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> productService.createProduct(productDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product with SKU IPHONE15PRO001 already exists");

        verify(productRepository).existsBySku("IPHONE15PRO001");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldGetProductByIdSuccessfully() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // When
        ProductDto result = productService.getProductById(1L);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("iPhone 15 Pro");
        verify(productRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFoundById() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product not found with id: 999");
    }

    @Test
    void shouldGetProductBySkuSuccessfully() {
        // Given
        when(productRepository.findBySku("IPHONE15PRO001")).thenReturn(Optional.of(product));

        // When
        ProductDto result = productService.getProductBySku("IPHONE15PRO001");

        // Then
        assertThat(result.getSku()).isEqualTo("IPHONE15PRO001");
        verify(productRepository).findBySku("IPHONE15PRO001");
    }

    @Test
    void shouldGetActiveProducts() {
        // Given
        List<Product> activeProducts = Arrays.asList(product);
        when(productRepository.findByIsActiveTrue()).thenReturn(activeProducts);

        // When
        List<ProductDto> result = productService.getActiveProducts();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
        verify(productRepository).findByIsActiveTrue();
    }

    @Test
    void shouldGetProductsByCategory() {
        // Given
        List<Product> categoryProducts = Arrays.asList(product);
        when(productRepository.findByCategoryIdAndIsActiveTrue(1L)).thenReturn(categoryProducts);

        // When
        List<ProductDto> result = productService.getProductsByCategory(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoryId()).isEqualTo(1L);
        verify(productRepository).findByCategoryIdAndIsActiveTrue(1L);
    }

    @Test
    void shouldGetFeaturedProducts() {
        // Given
        product.setIsFeatured(true);
        List<Product> featuredProducts = Arrays.asList(product);
        when(productRepository.findByIsFeaturedTrueAndIsActiveTrue()).thenReturn(featuredProducts);

        // When
        List<ProductDto> result = productService.getFeaturedProducts();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsFeatured()).isTrue();
        verify(productRepository).findByIsFeaturedTrueAndIsActiveTrue();
    }

    @Test
    void shouldGetProductsByBrand() {
        // Given
        List<Product> brandProducts = Arrays.asList(product);
        when(productRepository.findByBrandAndIsActiveTrue("Apple")).thenReturn(brandProducts);

        // When
        List<ProductDto> result = productService.getProductsByBrand("Apple");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBrand()).isEqualTo("Apple");
        verify(productRepository).findByBrandAndIsActiveTrue("Apple");
    }

    @Test
    void shouldSearchProductsByName() {
        // Given
        List<Product> searchResults = Arrays.asList(product);
        when(productRepository.findByNameContainingAndIsActiveTrue("iPhone")).thenReturn(searchResults);

        // When
        List<ProductDto> result = productService.searchProductsByName("iPhone");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).contains("iPhone");
        verify(productRepository).findByNameContainingAndIsActiveTrue("iPhone");
    }

    @Test
    void shouldGetProductsByPriceRange() {
        // Given
        BigDecimal minPrice = new BigDecimal("500.00");
        BigDecimal maxPrice = new BigDecimal("1500.00");
        List<Product> priceRangeProducts = Arrays.asList(product);
        when(productRepository.findByPriceBetweenAndIsActiveTrue(minPrice, maxPrice)).thenReturn(priceRangeProducts);

        // When
        List<ProductDto> result = productService.getProductsByPriceRange(minPrice, maxPrice);

        // Then
        assertThat(result).hasSize(1);
        verify(productRepository).findByPriceBetweenAndIsActiveTrue(minPrice, maxPrice);
    }

    @Test
    void shouldGetInStockProducts() {
        // Given
        List<Product> inStockProducts = Arrays.asList(product);
        when(productRepository.findInStockProducts()).thenReturn(inStockProducts);

        // When
        List<ProductDto> result = productService.getInStockProducts();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStockQuantity()).isGreaterThan(0);
        verify(productRepository).findInStockProducts();
    }

    @Test
    void shouldGetOutOfStockProducts() {
        // Given
        product.setStockQuantity(0);
        List<Product> outOfStockProducts = Arrays.asList(product);
        when(productRepository.findOutOfStockProducts()).thenReturn(outOfStockProducts);

        // When
        List<ProductDto> result = productService.getOutOfStockProducts();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStockQuantity()).isEqualTo(0);
        verify(productRepository).findOutOfStockProducts();
    }

    @Test
    void shouldUpdateProductSuccessfully() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDto updateDto = ProductTestDataBuilder.createProductDto();
        updateDto.setName("Updated iPhone");
        updateDto.setPrice(new BigDecimal("1199.99"));

        // When
        ProductDto result = productService.updateProduct(1L, updateDto);

        // Then
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldUpdateStockSuccessfully() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductDto result = productService.updateStock(1L, 100);

        // Then
        verify(productRepository).findById(1L);
        verify(productRepository).save(argThat(p -> p.getStockQuantity() == 100));
    }

    @Test
    void shouldDeleteProductSoftly() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        productService.deleteProduct(1L);

        // Then
        verify(productRepository).findById(1L);
        verify(productRepository).save(argThat(p -> !p.getIsActive()));
    }

    @Test
    void shouldCheckIfProductExistsBySku() {
        // Given
        when(productRepository.existsBySku("IPHONE15PRO001")).thenReturn(true);
        when(productRepository.existsBySku("NONEXISTENT")).thenReturn(false);

        // When & Then
        assertThat(productService.existsBySku("IPHONE15PRO001")).isTrue();
        assertThat(productService.existsBySku("NONEXISTENT")).isFalse();
    }
}