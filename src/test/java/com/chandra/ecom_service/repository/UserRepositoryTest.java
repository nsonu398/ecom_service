// src/test/java/com/chandra/ecom_service/repository/UserRepositoryTest.java
package com.chandra.ecom_service.repository;

import com.chandra.ecom_service.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("password123");
        user.setPhoneNumber("1234567890");
        user.setIsActive(true);
    }

    @Test
    void findByEmail_Success() {
        // Given
        entityManager.persistAndFlush(user);

        // When
        Optional<User> found = userRepository.findByEmail("john.doe@example.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
        assertEquals("john.doe@example.com", found.get().getEmail());
    }

    @Test
    void findByEmail_NotFound() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void existsByEmail_ReturnsTrue() {
        // Given
        entityManager.persistAndFlush(user);

        // When
        boolean exists = userRepository.existsByEmail("john.doe@example.com");

        // Then
        assertTrue(exists);
    }

    @Test
    void existsByEmail_ReturnsFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertFalse(exists);
    }

    @Test
    void findByEmailAndIsActiveTrue_Success() {
        // Given
        entityManager.persistAndFlush(user);

        // When
        Optional<User> found = userRepository.findByEmailAndIsActiveTrue("john.doe@example.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
        assertTrue(found.get().getIsActive());
    }

    @Test
    void findByEmailAndIsActiveTrue_InactiveUser_NotFound() {
        // Given
        user.setIsActive(false);
        entityManager.persistAndFlush(user);

        // When
        Optional<User> found = userRepository.findByEmailAndIsActiveTrue("john.doe@example.com");

        // Then
        assertFalse(found.isPresent());
    }
}