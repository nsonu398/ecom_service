// src/test/java/com/chandra/ecom_service/service/UserServiceImplTest.java
package com.chandra.ecom_service.service;

import com.chandra.ecom_service.dto.UserDto;
import com.chandra.ecom_service.entity.User;
import com.chandra.ecom_service.repository.UserRepository;
import com.chandra.ecom_service.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDto userDto;
    private User user;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
        userDto.setEmail("john.doe@example.com");
        userDto.setPhoneNumber("1234567890");

        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setPhoneNumber("1234567890");
        user.setPassword("password123");
        user.setIsActive(true);
    }

    @Test
    void createUser_Success() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        UserDto result = userService.createUser(userDto, "password123");

        // Then
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@example.com", result.getEmail());
        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_EmailAlreadyExists_ThrowsException() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.createUser(userDto, "password123"));

        assertEquals("User with email john.doe@example.com already exists",
                exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        UserDto result = userService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirstName());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.getUserById(1L));

        assertEquals("User not found with id: 1", exception.getMessage());
    }

    @Test
    void getUserByEmail_Success() {
        // Given
        when(userRepository.findByEmailAndIsActiveTrue(anyString()))
                .thenReturn(Optional.of(user));

        // When
        UserDto result = userService.getUserByEmail("john.doe@example.com");

        // Then
        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());
        verify(userRepository).findByEmailAndIsActiveTrue("john.doe@example.com");
    }

    @Test
    void getAllUsers_Success() {
        // Given
        User user2 = new User();
        user2.setId(2L);
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setEmail("jane.smith@example.com");
        user2.setIsActive(true);

        when(userRepository.findAll()).thenReturn(Arrays.asList(user, user2));

        // When
        List<UserDto> result = userService.getAllUsers();

        // Then
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Jane", result.get(1).getFirstName());
    }

    @Test
    void updateUser_Success() {
        // Given
        UserDto updateDto = new UserDto();
        updateDto.setFirstName("John Updated");
        updateDto.setLastName("Doe Updated");
        updateDto.setPhoneNumber("9876543210");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        UserDto result = userService.updateUser(1L, updateDto);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void existsByEmail_ReturnsTrue() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When
        boolean result = userService.existsByEmail("john.doe@example.com");

        // Then
        assertTrue(result);
        verify(userRepository).existsByEmail("john.doe@example.com");
    }
}