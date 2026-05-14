package com.example.pocproject.security.service;

import com.example.pocproject.security.model.ERole;
import com.example.pocproject.security.model.Role;
import com.example.pocproject.security.model.User;
import com.example.pocproject.security.payload.UpdateUserRequest;
import com.example.pocproject.security.payload.UserResponse;
import com.example.pocproject.security.repository.RoleRepository;
import com.example.pocproject.security.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for managing users (admin operations). */
@Service
public class UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Get all users.
   *
   * @return List of all users.
   */
  public List<UserResponse> getAllUsers() {
    return userRepository.findAll().stream()
        .map(this::convertToUserResponse)
        .collect(Collectors.toList());
  }

  /**
   * Get user by ID.
   *
   * @param id User ID.
   * @return User if found.
   */
  public Optional<UserResponse> getUserById(Long id) {
    return userRepository.findById(id).map(this::convertToUserResponse);
  }

  /**
   * Get user by username.
   *
   * @param username Username.
   * @return User if found.
   */
  public Optional<UserResponse> getUserByUsername(String username) {
    return userRepository.findByUsername(username).map(this::convertToUserResponse);
  }

  /**
   * Update user by ID.
   *
   * @param id User ID.
   * @param updateRequest Update request.
   * @return Updated user.
   */
  @Transactional
  public Optional<UserResponse> updateUser(Long id, UpdateUserRequest updateRequest) {
    return userRepository
        .findById(id)
        .map(
            user -> {
              if (updateRequest.getUsername() != null && !updateRequest.getUsername().isEmpty()) {
                // Check if username is already taken by another user
                if (userRepository.existsByUsername(updateRequest.getUsername())
                    && !user.getUsername().equals(updateRequest.getUsername())) {
                  throw new RuntimeException("Error: Username is already taken!");
                }
                user.setUsername(updateRequest.getUsername());
              }

              if (updateRequest.getPassword() != null && !updateRequest.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
              }

              if (updateRequest.getRoles() != null && !updateRequest.getRoles().isEmpty()) {
                Set<Role> roles = new HashSet<>();
                updateRequest
                    .getRoles()
                    .forEach(
                        roleName -> {
                          try {
                            Role role =
                                roleRepository
                                    .findByName(ERole.valueOf(roleName.toUpperCase()))
                                    .orElseThrow(
                                        () ->
                                            new RuntimeException(
                                                "Error: Role " + roleName + " is not found."));
                            roles.add(role);
                          } catch (IllegalArgumentException e) {
                            throw new RuntimeException("Error: Invalid role " + roleName);
                          }
                        });
                user.setRoles(roles);
              }

              User updatedUser = userRepository.save(user);
              return convertToUserResponse(updatedUser);
            });
  }

  /**
   * Delete user by ID.
   *
   * @param id User ID.
   * @return true if deleted, false if not found.
   */
  @Transactional
  public boolean deleteUser(Long id) {
    if (userRepository.existsById(id)) {
      userRepository.deleteById(id);
      return true;
    }
    return false;
  }

  /**
   * Convert User entity to UserResponse DTO.
   *
   * @param user User entity.
   * @return UserResponse DTO.
   */
  private UserResponse convertToUserResponse(User user) {
    Set<String> roles =
        user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet());
    return new UserResponse(user.getId(), user.getUsername(), roles);
  }
}
