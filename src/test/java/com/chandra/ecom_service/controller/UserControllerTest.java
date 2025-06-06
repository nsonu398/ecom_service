// src/test/java/com/chandra/ecom_service/controller/UserControllerTest.java
package com.chandra.ecom_service.controller;

import com.chandra.ecom_service.config.TestSecurityConfig;
import com.chandra.ecom_service.dto.CreateUserRequest;
import com.chandra.ecom_service.dto.UserDto;
import com.chandra.ecom_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings({"deprecation", "removal"})  // Suppress the MockBean deprecation warning
@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto userDto;
    private CreateUserRequest createUserRequest;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
        userDto.setEmail("john.doe@example.com");
        userDto.setPhoneNumber("1234567890");

        createUserRequest = new CreateUserRequest();
        createUserRequest.setFirstName("John");
        createUserRequest.setLastName("Doe");
        createUserRequest.setEmail("john.doe@example.com");
        createUserRequest.setPassword("password123");
        createUserRequest.setPhoneNumber("1234567890");
    }

    @Test
    void createUser_Success() throws Exception {
        // Given
        when(userService.createUser(any(UserDto.class), anyString())).thenReturn(userDto);

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void createUser_InvalidInput_BadRequest() throws Exception {
        // Given - Invalid request (missing required fields)
        CreateUserRequest invalidRequest = new CreateUserRequest();
        invalidRequest.setEmail("invalid-email"); // Invalid email format

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_Success() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(userDto);

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void getUserByEmail_Success() throws Exception {
        // Given
        when(userService.getUserByEmail("john.doe@example.com")).thenReturn(userDto);

        // When & Then
        mockMvc.perform(get("/api/users/email/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void getAllUsers_Success() throws Exception {
        // Given
        UserDto userDto2 = new UserDto();
        userDto2.setId(2L);
        userDto2.setFirstName("Jane");
        userDto2.setLastName("Smith");
        userDto2.setEmail("jane.smith@example.com");

        List<UserDto> users = Arrays.asList(userDto, userDto2);
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));
    }

    @Test
    void updateUser_Success() throws Exception {
        // Given
        when(userService.updateUser(any(Long.class), any(UserDto.class))).thenReturn(userDto);

        // When & Then
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteUser_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void checkEmailExists_ReturnsTrue() throws Exception {
        // Given
        when(userService.existsByEmail("john.doe@example.com")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/users/exists/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}