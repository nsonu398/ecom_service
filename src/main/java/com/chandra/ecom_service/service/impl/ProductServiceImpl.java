// src/main/java/com/chandra/ecom_service/service/impl/ProductServiceImpl.java
package com.chandra.ecom_service.service.impl;

import com.chandra.ecom_service.dto.ProductDto;
import com.chandra.ecom_service.entity.Product;
import com.chandra.ecom_service.repository.ProductRepository;
import com.chandra.ecom_service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        if (existsBySku(productDto.getSku())) {
            throw new RuntimeException("Product with SKU " + productDto.getSku() + " already exists");
        }

        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setSku(productDto.getSku());
        product.setCategoryId(productDto.getCategoryId());
        product.setBrand(productDto.getBrand());
        product.setStockQuantity(productDto.getStockQuantity() != null ? productDto.getStockQuantity() : 0);
        product.setWeight(productDto.getWeight());
        product.setDimensions(productDto.getDimensions());
        product.setColor(productDto.getColor());
        product.setSize(productDto.getSize());

        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }

    @Override
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return convertToDto(product);
    }

    @Override
    public ProductDto getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Product not found with SKU: " + sku));
        return convertToDto(product);
    }

    @Override
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> getActiveProducts() {
        return productRepository.findByIsActiveTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrueAndIsActiveTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> getProductsByBrand(String brand) {
        return productRepository.findByBrandAndIsActiveTrue(brand).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> searchProductsByName(String name) {
        return productRepository.findByNameContainingAndIsActiveTrue(name).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetweenAndIsActiveTrue(minPrice, maxPrice).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> getInStockProducts() {
        return productRepository.findInStockProducts().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setCategoryId(productDto.getCategoryId());
        product.setBrand(productDto.getBrand());
        product.setStockQuantity(productDto.getStockQuantity());
        product.setWeight(productDto.getWeight());
        product.setDimensions(productDto.getDimensions());
        product.setColor(productDto.getColor());
        product.setSize(productDto.getSize());
        product.setIsFeatured(productDto.getIsFeatured());
        // SKU is not updated to maintain uniqueness

        Product updatedProduct = productRepository.save(product);
        return convertToDto(updatedProduct);
    }

    @Override
    public ProductDto updateStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setStockQuantity(quantity);
        Product updatedProduct = productRepository.save(product);
        return convertToDto(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Soft delete
        product.setIsActive(false);
        productRepository.save(product);
    }

    @Override
    public boolean existsBySku(String sku) {
        return productRepository.existsBySku(sku);
    }

    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setSku(product.getSku());
        dto.setCategoryId(product.getCategoryId());
        dto.setBrand(product.getBrand());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setWeight(product.getWeight());
        dto.setDimensions(product.getDimensions());
        dto.setColor(product.getColor());
        dto.setSize(product.getSize());
        dto.setIsActive(product.getIsActive());
        dto.setIsFeatured(product.getIsFeatured());
        return dto;
    }
}