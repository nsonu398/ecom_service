// src/test/java/com/chandra/ecom_service/repository/ProductRepositoryTest.java
package com.chandra.ecom_service.repository;

import com.chandra.ecom_service.entity.Product;
import com.chandra.ecom_service.testutils.ProductTestDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldFindProductBySku() {
        // Given
        Product product = ProductTestDataBuilder.createProductEntity();
        entityManager.persistAndFlush(product);

        // When
        Optional<Product> found = productRepository.findBySku("IPHONE15PRO001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("iPhone 15 Pro");
        assertThat(found.get().getSku()).isEqualTo("IPHONE15PRO001");
    }

    @Test
    void shouldCheckIfProductExistsBySku() {
        // Given
        Product product = ProductTestDataBuilder.createProductEntity();
        entityManager.persistAndFlush(product);

        // When & Then
        assertThat(productRepository.existsBySku("IPHONE15PRO001")).isTrue();
        assertThat(productRepository.existsBySku("NONEXISTENT")).isFalse();
    }

    @Test
    void shouldFindActiveProducts() {
        // Given
        Product activeProduct = ProductTestDataBuilder.createProductEntity();
        Product inactiveProduct = ProductTestDataBuilder.createProductEntity();
        inactiveProduct.setSku("INACTIVE001");
        inactiveProduct.setIsActive(false);

        entityManager.persistAndFlush(activeProduct);
        entityManager.persistAndFlush(inactiveProduct);

        // When
        List<Product> activeProducts = productRepository.findByIsActiveTrue();

        // Then
        assertThat(activeProducts).hasSize(1);
        assertThat(activeProducts.get(0).getSku()).isEqualTo("IPHONE15PRO001");
    }

    @Test
    void shouldFindProductsByCategory() {
        // Given
        Product product1 = ProductTestDataBuilder.createProductEntityWithCategory(1L);
        Product product2 = ProductTestDataBuilder.createProductEntityWithCategory(1L);
        product2.setSku("PRODUCT2");
        Product product3 = ProductTestDataBuilder.createProductEntityWithCategory(2L);
        product3.setSku("PRODUCT3");

        entityManager.persistAndFlush(product1);
        entityManager.persistAndFlush(product2);
        entityManager.persistAndFlush(product3);

        // When
        List<Product> categoryProducts = productRepository.findByCategoryIdAndIsActiveTrue(1L);

        // Then
        assertThat(categoryProducts).hasSize(2);
        assertThat(categoryProducts).extracting("categoryId").containsOnly(1L);
    }

    @Test
    void shouldFindFeaturedProducts() {
        // Given
        Product featuredProduct = ProductTestDataBuilder.createProductEntity();
        featuredProduct.setIsFeatured(true);
        Product regularProduct = ProductTestDataBuilder.createProductEntity();
        regularProduct.setSku("REGULAR001");
        regularProduct.setIsFeatured(false);

        entityManager.persistAndFlush(featuredProduct);
        entityManager.persistAndFlush(regularProduct);

        // When
        List<Product> featuredProducts = productRepository.findByIsFeaturedTrueAndIsActiveTrue();

        // Then
        assertThat(featuredProducts).hasSize(1);
        assertThat(featuredProducts.get(0).getIsFeatured()).isTrue();
    }

    @Test
    void shouldFindProductsByBrand() {
        // Given
        Product appleProduct = ProductTestDataBuilder.createProductEntity();
        Product samsungProduct = ProductTestDataBuilder.createProductEntity();
        samsungProduct.setSku("SAMSUNG001");
        samsungProduct.setBrand("Samsung");

        entityManager.persistAndFlush(appleProduct);
        entityManager.persistAndFlush(samsungProduct);

        // When
        List<Product> appleProducts = productRepository.findByBrandAndIsActiveTrue("Apple");

        // Then
        assertThat(appleProducts).hasSize(1);
        assertThat(appleProducts.get(0).getBrand()).isEqualTo("Apple");
    }

    @Test
    void shouldFindProductsByNameContaining() {
        // Given
        Product iphone = ProductTestDataBuilder.createProductEntity();
        Product ipad = ProductTestDataBuilder.createProductEntity();
        ipad.setSku("IPAD001");
        ipad.setName("iPad Pro");

        entityManager.persistAndFlush(iphone);
        entityManager.persistAndFlush(ipad);

        // When
        List<Product> products = productRepository.findByNameContainingAndIsActiveTrue("iPhone");

        // Then
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).contains("iPhone");
    }

    @Test
    void shouldFindProductsByPriceRange() {
        // Given
        Product cheapProduct = ProductTestDataBuilder.createProductEntity();
        cheapProduct.setPrice(new BigDecimal("500.00"));
        cheapProduct.setSku("CHEAP001");

        Product expensiveProduct = ProductTestDataBuilder.createProductEntity();
        expensiveProduct.setPrice(new BigDecimal("1500.00"));
        expensiveProduct.setSku("EXPENSIVE001");

        entityManager.persistAndFlush(cheapProduct);
        entityManager.persistAndFlush(expensiveProduct);

        // When
        List<Product> productsInRange = productRepository.findByPriceBetweenAndIsActiveTrue(
                new BigDecimal("400.00"), new BigDecimal("1000.00"));

        // Then
        assertThat(productsInRange).hasSize(1);
        assertThat(productsInRange.get(0).getPrice()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    void shouldFindInStockProducts() {
        // Given
        Product inStockProduct = ProductTestDataBuilder.createProductEntity();
        inStockProduct.setStockQuantity(10);

        Product outOfStockProduct = ProductTestDataBuilder.createProductEntity();
        outOfStockProduct.setSku("OUTOFSTOCK001");
        outOfStockProduct.setStockQuantity(0);

        entityManager.persistAndFlush(inStockProduct);
        entityManager.persistAndFlush(outOfStockProduct);

        // When
        List<Product> inStockProducts = productRepository.findInStockProducts();

        // Then
        assertThat(inStockProducts).hasSize(1);
        assertThat(inStockProducts.get(0).getStockQuantity()).isGreaterThan(0);
    }

    @Test
    void shouldFindOutOfStockProducts() {
        // Given
        Product inStockProduct = ProductTestDataBuilder.createProductEntity();
        inStockProduct.setStockQuantity(10);

        Product outOfStockProduct = ProductTestDataBuilder.createProductEntity();
        outOfStockProduct.setSku("OUTOFSTOCK001");
        outOfStockProduct.setStockQuantity(0);

        entityManager.persistAndFlush(inStockProduct);
        entityManager.persistAndFlush(outOfStockProduct);

        // When
        List<Product> outOfStockProducts = productRepository.findOutOfStockProducts();

        // Then
        assertThat(outOfStockProducts).hasSize(1);
        assertThat(outOfStockProducts.get(0).getStockQuantity()).isEqualTo(0);
    }
}