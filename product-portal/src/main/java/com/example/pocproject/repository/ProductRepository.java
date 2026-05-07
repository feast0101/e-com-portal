package com.example.pocproject.repository;

import com.example.pocproject.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link Product} entity.
 * Provides standard CRUD operations and custom query capabilities.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
