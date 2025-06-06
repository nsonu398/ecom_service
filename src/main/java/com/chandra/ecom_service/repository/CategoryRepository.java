// src/main/java/com/chandra/ecom_service/repository/CategoryRepository.java
package com.chandra.ecom_service.repository;

import com.chandra.ecom_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    List<Category> findByIsActiveTrue();

    List<Category> findByParentCategoryIdAndIsActiveTrue(Long parentCategoryId);

    List<Category> findByParentCategoryIdIsNullAndIsActiveTrue(); // Root categories
}