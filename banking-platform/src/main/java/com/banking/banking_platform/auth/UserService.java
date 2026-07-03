package com.banking.banking_platform.auth;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

  @Transactional
  public AuthResponse register(RegisterRequest request) {
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

    return new AuthResponse("dummy-token", savedUser.getEmail(), savedUser.getRole());
  }

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
    }
    return new AuthResponse("dummy-token", user.getEmail(), user.getRole());
  }

  @Transactional(readOnly = true)
  public UserResponse getCurrentUser(String email) {
    return toUserResponse(
        userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
  }

  @Transactional
  public UserResponse createUser(CreateUserRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists.");
    }

    User user = new User();
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRole(request.getRole());
    user.setIsActive(true);

    User savedUser = userRepository.save(user);
    return toUserResponse(savedUser);
  }

  @Transactional(readOnly = true)
  public List<UserResponse> getAllUsers() {
    return userRepository.findAll().stream().map(this::toUserResponse).toList();
  }

  @Transactional(readOnly = true)
  public UserResponse getUserById(UUID id) {
    return toUserResponse(
        userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
  }

  @Transactional
  public UserResponse deactivateUserById(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    if (!user.getIsActive()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already inactive");
    }

    user.setIsActive(false);
    User savedUser = userRepository.save(user);

    return toUserResponse(savedUser);
  }

  @Transactional
  public UserResponse activateUserById(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    if (user.getIsActive()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already active");
    }

    user.setIsActive(true);
    User savedUser = userRepository.save(user);

    return toUserResponse(savedUser);
  }

  private UserResponse toUserResponse(User user) {
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
