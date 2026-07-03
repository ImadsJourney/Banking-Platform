package com.banking.banking_platform.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.banking.banking_platform.auth.dto.AuthResponse;
import com.banking.banking_platform.auth.dto.CreateUserRequest;
import com.banking.banking_platform.auth.dto.LoginRequest;
import com.banking.banking_platform.auth.dto.RegisterRequest;
import com.banking.banking_platform.auth.dto.UserResponse;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
  @Mock
  UserRepository repository;

  @Mock
  PasswordEncoder passwordEncoder;

  @InjectMocks
  UserService service;

  /*
   * helper method that provides a test user and request before each test
   * every method has a fresh object, so there are no overwrites
   */

  private User testUser;
  private RegisterRequest registerRequest;
  private LoginRequest loginRequest;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(UUID.randomUUID());
    testUser.setFirstName("Max");
    testUser.setLastName("Mustermann");
    testUser.setEmail("max@mustermann.com");
    testUser.setPassword("hashed-password");
    testUser.setRole(Role.CUSTOMER);
    testUser.setIsActive(true);

    registerRequest = new RegisterRequest();
    registerRequest.setFirstName("Max");
    registerRequest.setLastName("Mustermann");
    registerRequest.setEmail("max@mustermann.com");
    registerRequest.setPassword("password123");

    loginRequest = new LoginRequest();
    loginRequest.setEmail("max@mustermann.com");
    loginRequest.setPassword("password123");
  }

  /*
   *
   * register + login tests
   */

  @Test
  void shouldRegisterUser() {
    when(repository.existsByEmail("max@mustermann.com")).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
    when(repository.save(any(User.class))).thenReturn(testUser);

    AuthResponse response = service.register(registerRequest);

    assertThat(response.token()).isEqualTo("dummy-token");
    assertThat(response.email()).isEqualTo("max@mustermann.com");
    assertThat(response.role()).isEqualTo(Role.CUSTOMER);

    verify(repository).save(any(User.class));
    verify(passwordEncoder).encode("password123");
    verify(repository).existsByEmail("max@mustermann.com");
  }

  @Test
  void shouldLoginUser() {
    when(repository.findByEmail("max@mustermann.com")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);

    AuthResponse response = service.login(loginRequest);

    assertThat(response.email()).isEqualTo("max@mustermann.com");
    assertThat(response.role()).isEqualTo(Role.CUSTOMER);
    assertThat(response.token()).isEqualTo("dummy-token");

    verify(repository).findByEmail("max@mustermann.com");
    verify(passwordEncoder).matches("password123", "hashed-password");
  }

  @Test
  void shouldNotAllowRegistrationBecauseUserExists() {
    when(repository.existsByEmail("max@mustermann.com")).thenReturn(true);

    assertThatThrownBy(() -> service.register(registerRequest)).isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("User already exists.");

    verify(repository).existsByEmail("max@mustermann.com");
  }

  @Test
  void shouldNotAllowLoginBecauseInvalidInfo() {
    LoginRequest request = new LoginRequest();
    request.setEmail("max@mustermann.com");
    request.setPassword("wrongPassword");

    when(repository.findByEmail("max@mustermann.com")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("wrongPassword", "hashed-password")).thenReturn(false);

    assertThatThrownBy(() -> service.login(request))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Invalid email or password.");

    verify(repository).findByEmail("max@mustermann.com");
    verify(passwordEncoder).matches("wrongPassword", "hashed-password");
  }

  /*
   *
   * Methods for admin/advisor are being tested here
   */

  @Test
  void shouldReturnAllUsers() {
    User testUser2 = new User();
    testUser2.setId(UUID.randomUUID());
    testUser2.setFirstName("Max");
    testUser2.setLastName("Mustermann");
    testUser2.setEmail("max2@mustermann.com");
    testUser2.setPassword("hashed-password");
    testUser2.setRole(Role.CUSTOMER);
    testUser2.setIsActive(true);

    User testUser3 = new User();
    testUser3.setId(UUID.randomUUID());
    testUser3.setFirstName("Max");
    testUser3.setLastName("Mustermann");
    testUser3.setEmail("max3@mustermann.com");
    testUser3.setPassword("hashed-password");
    testUser3.setRole(Role.CUSTOMER);
    testUser3.setIsActive(true);

    when(repository.findAll()).thenReturn(List.of(testUser, testUser2, testUser3));

    List<UserResponse> response = service.getAllUsers();

    assertEquals(3, response.size());
    assertEquals("max2@mustermann.com", response.get(1).email());

    verify(repository, times(1)).findAll();

  }

  @Test
  void shouldReturnCurrentUser() {
    when(repository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

    UserResponse response = service.getCurrentUser("max@mustermann.com");

    assertThat(response.email()).isEqualTo("max@mustermann.com");
    assertThat(response.firstName()).isEqualTo("Max");
    assertThat(response.lastName()).isEqualTo("Mustermann");
    assertThat(response.role()).isEqualTo(Role.CUSTOMER);
    assertThat(response.isActive()).isTrue();

    verify(repository).findByEmail("max@mustermann.com");
  }

  @Test
  void shouldThrowWhenGetCurrentUserNotFound() {
    when(repository.findByEmail("unknown@email.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getCurrentUser("unknown@email.com"))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("User not found");

    verify(repository).findByEmail("unknown@email.com");
  }

  @Test
  void shouldCreateUser() {
    CreateUserRequest createRequest = new CreateUserRequest();
    createRequest.setFirstName("Admin");
    createRequest.setLastName("User");
    createRequest.setEmail("admin@banking.com");
    createRequest.setPassword("admin123");
    createRequest.setRole(Role.ADMIN);

    when(repository.existsByEmail("admin@banking.com")).thenReturn(false);
    when(passwordEncoder.encode("admin123")).thenReturn("hashed-admin-password");

    User adminUser = new User();
    adminUser.setId(UUID.randomUUID());
    adminUser.setFirstName("Admin");
    adminUser.setLastName("User");
    adminUser.setEmail("admin@banking.com");
    adminUser.setPassword("hashed-admin-password");
    adminUser.setRole(Role.ADMIN);
    adminUser.setIsActive(true);

    when(repository.save(any(User.class))).thenReturn(adminUser);

    UserResponse response = service.createUser(createRequest);

    assertThat(response.email()).isEqualTo("admin@banking.com");
    assertThat(response.role()).isEqualTo(Role.ADMIN);
    assertThat(response.isActive()).isTrue();

    verify(repository).existsByEmail("admin@banking.com");
    verify(passwordEncoder).encode("admin123");
    verify(repository).save(any(User.class));
  }

  @Test
  void shouldThrowWhenCreateUserWithDuplicateEmail() {
    CreateUserRequest createRequest = new CreateUserRequest();
    createRequest.setEmail("existing@banking.com");

    when(repository.existsByEmail("existing@banking.com")).thenReturn(true);

    assertThatThrownBy(() -> service.createUser(createRequest))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("User already exists.");

    verify(repository).existsByEmail("existing@banking.com");
  }

  @Test
  void shouldGetUserById() {
    UUID userId = testUser.getId();
    when(repository.findById(userId)).thenReturn(Optional.of(testUser));

    UserResponse response = service.getUserById(userId);

    assertThat(response.email()).isEqualTo("max@mustermann.com");
    assertThat(response.id()).isEqualTo(userId);
    assertThat(response.role()).isEqualTo(Role.CUSTOMER);

    verify(repository).findById(userId);
  }

  @Test
  void shouldThrowWhenGetUserByIdNotFound() {
    UUID unknownId = UUID.randomUUID();
    when(repository.findById(unknownId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getUserById(unknownId))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("User not found");

    verify(repository).findById(unknownId);
  }

  @Test
  void shouldDeactivateUser() {
    UUID userId = testUser.getId();
    when(repository.findById(userId)).thenReturn(Optional.of(testUser));

    User deactivatedUser = new User();
    deactivatedUser.setId(userId);
    deactivatedUser.setFirstName("Max");
    deactivatedUser.setLastName("Mustermann");
    deactivatedUser.setEmail("max@mustermann.com");
    deactivatedUser.setPassword("hashed-password");
    deactivatedUser.setRole(Role.CUSTOMER);
    deactivatedUser.setIsActive(false);

    when(repository.save(any(User.class))).thenReturn(deactivatedUser);

    UserResponse response = service.deactivateUserById(userId);

    assertThat(response.isActive()).isFalse();
    assertThat(response.email()).isEqualTo("max@mustermann.com");

    verify(repository).findById(userId);
    verify(repository).save(any(User.class));
  }

  @Test
  void shouldThrowWhenDeactivateAlreadyInactiveUser() {
    UUID userId = testUser.getId();

    User inactiveUser = new User();
    inactiveUser.setId(userId);
    inactiveUser.setEmail("max@mustermann.com");
    inactiveUser.setIsActive(false);

    when(repository.findById(userId)).thenReturn(Optional.of(inactiveUser));

    assertThatThrownBy(() -> service.deactivateUserById(userId))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("User is already inactive");

    verify(repository).findById(userId);
  }

  @Test
  void shouldActivateUser() {
    UUID userId = UUID.randomUUID();

    User inactiveUser = new User();
    inactiveUser.setId(userId);
    inactiveUser.setFirstName("Max");
    inactiveUser.setLastName("Mustermann");
    inactiveUser.setEmail("max@mustermann.com");
    inactiveUser.setPassword("hashed-password");
    inactiveUser.setRole(Role.CUSTOMER);
    inactiveUser.setIsActive(false);

    when(repository.findById(userId)).thenReturn(Optional.of(inactiveUser));

    User activatedUser = new User();
    activatedUser.setId(userId);
    activatedUser.setFirstName("Max");
    activatedUser.setLastName("Mustermann");
    activatedUser.setEmail("max@mustermann.com");
    activatedUser.setPassword("hashed-password");
    activatedUser.setRole(Role.CUSTOMER);
    activatedUser.setIsActive(true);

    when(repository.save(any(User.class))).thenReturn(activatedUser);

    UserResponse response = service.activateUserById(userId);

    assertThat(response.isActive()).isTrue();
    assertThat(response.email()).isEqualTo("max@mustermann.com");

    verify(repository).findById(userId);
    verify(repository).save(any(User.class));
  }

  @Test
  void shouldThrowWhenActivateAlreadyActiveUser() {
    UUID userId = testUser.getId();
    when(repository.findById(userId)).thenReturn(Optional.of(testUser));

    assertThatThrownBy(() -> service.activateUserById(userId))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("User is already active");

    verify(repository).findById(userId);
  }

  @Test
  void shouldThrowWhenDeactivateNonExistentUser() {
    UUID unknownId = UUID.randomUUID();
    when(repository.findById(unknownId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.deactivateUserById(unknownId))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("User not found");

    verify(repository).findById(unknownId);
  }

  @Test
  void shouldThrowWhenActivateNonExistentUser() {
    UUID unknownId = UUID.randomUUID();
    when(repository.findById(unknownId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.activateUserById(unknownId))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("User not found");

    verify(repository).findById(unknownId);
  }
}
