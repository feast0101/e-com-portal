package com.example.pocproject.security.config;

import com.example.pocproject.security.filter.JwtRequestFilter;
import com.example.pocproject.security.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration class. Configures security filters, authentication providers, and
 * authorization rules.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

  private final UserDetailsServiceImpl userDetailsService;
  private final JwtRequestFilter jwtRequestFilter;

  public SecurityConfig(
      UserDetailsServiceImpl userDetailsService, JwtRequestFilter jwtRequestFilter) {
    this.userDetailsService = userDetailsService;
    this.jwtRequestFilter = jwtRequestFilter;
  }

  /**
   * Provides a BCryptPasswordEncoder bean for password hashing.
   *
   * @return A BCryptPasswordEncoder instance.
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Configures the DaoAuthenticationProvider with custom UserDetailsService and PasswordEncoder.
   *
   * @return A configured DaoAuthenticationProvider.
   */
  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  /**
   * Provides the AuthenticationManager bean.
   *
   * @param authConfig The AuthenticationConfiguration.
   * @return The AuthenticationManager instance.
   * @throws Exception if an error occurs during configuration.
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
      throws Exception {
    return authConfig.getAuthenticationManager();
  }

  /**
   * Configures the security filter chain. Defines authorization rules, session management, and adds
   * the JWT filter.
   *
   * @param http The HttpSecurity object to configure.
   * @return The configured SecurityFilterChain.
   * @throws Exception if an error occurs during configuration.
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/")
                    .permitAll() // Allow root path
                    .requestMatchers("/api/auth/**")
                    .permitAll() // Allow authentication endpoints
                    .requestMatchers("/h2-console/**")
                    .permitAll() // Allow H2 console access
                    .requestMatchers("/actuator/**")
                    .permitAll() // Allow actuator endpoints
                    .requestMatchers("/api/products/retry-demo")
                    .permitAll() // Allow retry demo without auth
                    .requestMatchers("/api/products/async-demo")
                    .permitAll() // Allow async demo without auth
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html")
                    .permitAll() // Allow Swagger UI
                    .requestMatchers("/api/admin/**")
                    .authenticated() // Admin endpoints require authentication (role check via
                    // @PreAuthorize)
                    .anyRequest()
                    .authenticated() // All other requests require authentication
            );

    http.headers(
        headers -> headers.frameOptions(frameOptions -> frameOptions.disable())); // For H2 console

    http.authenticationProvider(authenticationProvider());
    http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
