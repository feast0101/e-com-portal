package com.example.pocproject.security.payload;

import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 * Request DTO for updating user information by admin.
 */
public class UpdateUserRequest {

    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    private String password;

    private Set<String> roles;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
