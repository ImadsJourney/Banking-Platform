package com.banking.banking_platform.auth;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.banking.banking_platform.auth.dto.AuthResponse;
import com.banking.banking_platform.auth.dto.CreateUserRequest;
import com.banking.banking_platform.auth.dto.LoginRequest;
import com.banking.banking_platform.auth.dto.RegisterRequest;
import com.banking.banking_platform.auth.dto.UserResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  /*
   * The login/register Methods are primarly for the auhtentication process
   */

  AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists.");
    }

    User user = new User();
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRole(Role.CUSTOMER);
    user.setIsActive(true);

    User savedUser = userRepository.save(user);

    return new AuthResponse("test-token", savedUser.getEmail(), savedUser.getRole());
  }

  AuthResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
    }
    return new AuthResponse("test-token", user.getEmail(), user.getRole());
  }

  /*
   * Method for reviewing your own profile and data
   */
  UserResponse getCurrentUser(String email) {
    return toUserResponse(userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException()));
  }

  /*
   * The following methods are for the admin/advisor
   */

  public UserResponse createUser(CreateUserRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT);
    }

    User user = new User();
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRole(request.getRole()); // Rolle kommt vom Admin
    user.setIsActive(true);

    User savedUser = userRepository.save(user);
    return toUserResponse(savedUser);
  }

  List<UserResponse> getAllUsers() {
    return userRepository.findAll().stream().map(user -> toUserResponse(user)).toList();
  }

  UserResponse getUserById(UUID id) {
    return toUserResponse(userRepository.findById(id).orElseThrow(() -> new RuntimeException()));
  }

  UserResponse deactivateUserById(UUID id) {

    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

    if (!user.getIsActive()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    user.setIsActive(false);
    User savedUser = userRepository.save(user);

    return new UserResponse(
        savedUser.getId(),
        savedUser.getFirstName(),
        savedUser.getLastName(),
        savedUser.getEmail(),
        savedUser.getRole(),
        savedUser.getCreatedAt(),
        savedUser.getIsActive());
  }

  UserResponse activateUserById(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (user.getIsActive() == true) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    user.setIsActive(true);
    User savedUser = userRepository.save(user);

    return new UserResponse(
        savedUser.getId(),
        savedUser.getFirstName(),
        savedUser.getLastName(),
        savedUser.getEmail(),
        savedUser.getRole(),
        savedUser.getCreatedAt(),
        savedUser.getIsActive());
  }

  UserResponse toUserResponse(User user) {
    return new UserResponse(
        user.getId(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        user.getRole(),
        user.getCreatedAt(),
        user.getIsActive());
  }

}
