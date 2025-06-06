// src/main/java/com/chandra/ecom_service/repository/ProductRepository.java
package com.chandra.ecom_service.repository;

import com.chandra.ecom_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Product> findByIsActiveTrue();

    List<Product> findByCategoryIdAndIsActiveTrue(Long categoryId);

    List<Product> findByIsFeaturedTrueAndIsActiveTrue();

    List<Product> findByBrandAndIsActiveTrue(String brand);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name% AND p.isActive = true")
    List<Product> findByNameContainingAndIsActiveTrue(@Param("name") String name);

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.isActive = true")
    List<Product> findByPriceBetweenAndIsActiveTrue(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.isActive = true")
    List<Product> findInStockProducts();

    @Query("SELECT p FROM Product p WHERE p.stockQuantity = 0 AND p.isActive = true")
    List<Product> findOutOfStockProducts();
}