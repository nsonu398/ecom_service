// src/main/java/com/chandra/ecom_service/service/UserService.java
package com.chandra.ecom_service.service;

import com.chandra.ecom_service.dto.UserDto;
import java.util.List;

public interface UserService {

    UserDto createUser(UserDto userDto, String password);

    UserDto getUserById(Long id);

    UserDto getUserByEmail(String email);

    List<UserDto> getAllUsers();

    UserDto updateUser(Long id, UserDto userDto);

    void deleteUser(Long id);

    boolean existsByEmail(String email);
}