package com.example.pocproject.security.controller;

import com.example.pocproject.security.model.ERole;
import com.example.pocproject.security.model.Role;
import com.example.pocproject.security.model.User;
import com.example.pocproject.security.payload.JwtResponse;
import com.example.pocproject.security.payload.LoginRequest;
import com.example.pocproject.security.payload.MessageResponse;
import com.example.pocproject.security.payload.SignupRequest;
import com.example.pocproject.security.repository.RoleRepository;
import com.example.pocproject.security.repository.UserRepository;
import com.example.pocproject.security.service.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user authentication and registration. Handles user signup and signin
 * requests, issuing JWT tokens upon successful authentication.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder encoder;
  private final JwtUtil jwtUtil;

  public AuthController(
      AuthenticationManager authenticationManager,
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder encoder,
      JwtUtil jwtUtil) {
    this.authenticationManager = authenticationManager;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.encoder = encoder;
    this.jwtUtil = jwtUtil;
  }

  /**
   * Authenticates a user and returns a JWT token.
   *
   * @param loginRequest The login request containing username and password.
   * @return A ResponseEntity with JwtResponse containing the JWT token and user details.
   */
  @Operation(
      summary = "Sign in",
      description =
          "Authenticates a user and returns a JWT token for accessing protected endpoints")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Authentication successful",
            content = @Content(schema = @Schema(implementation = JwtResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
      })
  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtil.generateJwtToken(authentication);

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    List<String> roles =
        userDetails.getAuthorities().stream()
            .map(item -> item.getAuthority())
            .collect(Collectors.toList());

    User user =
        userRepository
            .findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found."));

    return ResponseEntity.ok(new JwtResponse(jwt, user.getId(), userDetails.getUsername(), roles));
  }

  /**
   * Registers a new user.
   *
   * @param signupRequest The signup request containing username, password, and optional roles.
   * @return A ResponseEntity with a MessageResponse indicating success or failure.
   */
  @Operation(
      summary = "Sign up",
      description = "Registers a new user account. Default role is ROLE_USER unless specified")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Username already exists or invalid data")
      })
  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
    if (userRepository.existsByUsername(signupRequest.getUsername())) {
      return ResponseEntity.badRequest()
          .body(new MessageResponse("Error: Username is already taken!"));
    }

    // Create new user's account
    User user = new User(signupRequest.getUsername(), encoder.encode(signupRequest.getPassword()));

    Set<String> strRoles = signupRequest.getRole();
    Set<Role> roles = new HashSet<>();

    if (strRoles == null) {
      Role userRole =
          roleRepository
              .findByName(ERole.ROLE_USER)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
      roles.add(userRole);
    } else {
      strRoles.forEach(
          role -> {
            switch (role) {
              case "admin":
                Role adminRole =
                    roleRepository
                        .findByName(ERole.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(adminRole);
                break;
              default:
                Role userRole =
                    roleRepository
                        .findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            }
          });
    }

    user.setRoles(roles);
    userRepository.save(user);

    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }
}
