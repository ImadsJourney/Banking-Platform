package com.banking.banking_platform.auth;

import java.util.List;
import java.util.UUID;

import org.springframework.boot.actuate.web.exchanges.HttpExchange.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banking.banking_platform.auth.dto.AuthResponse;
import com.banking.banking_platform.auth.dto.CreateUserRequest;
import com.banking.banking_platform.auth.dto.LoginRequest;
import com.banking.banking_platform.auth.dto.RegisterRequest;
import com.banking.banking_platform.auth.dto.UserResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest request) {
    return userService.login(request);
  }

  @PostMapping("/register")
  public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
    return userService.register(request);
  }

  @PostMapping
  public UserResponse createNewUser(@Valid @RequestBody CreateUserRequest userRequest) {
    return userService.createUser(userRequest);
  }

  @GetMapping
  public List<UserResponse> getAllUserResponses() {
    return userService.getAllUsers();
  }

  @GetMapping("/me")
  public UserResponse getCurrentUser(Principal principal) {
    return userService.getCurrentUser(principal.getName());
  }

  @GetMapping("/{id}")
  public UserResponse getUserById(@PathVariable UUID id) {
    return userService.getUserById(id);
  }

  @PatchMapping("/{id}/deactivate")
  public UserResponse deactivateUser(@PathVariable UUID id) {
    return userService.deactivateUserById(id);
  }

  @PatchMapping("/{id}/activate")
  public UserResponse activateUser(@PathVariable UUID id) {
    return userService.activateUserById(id);
  }
}
