package com.example.pocproject.security.repository;

import com.example.pocproject.security.model.ERole;
import com.example.pocproject.security.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link Role} entity.
 * Provides methods for role data access, including finding by role name.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
