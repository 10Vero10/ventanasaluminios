package com.ventanasyaluminios.repository;

import com.ventanasyaluminios.entity.Product;
import com.ventanasyaluminios.entity.enums.ProductCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Acceso al catálogo de productos. */
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByOrderByPositionAsc();

    List<Product> findByCategoryOrderByPositionAsc(ProductCategory category);

    List<Product> findByActiveTrueOrderByPositionAsc();

    List<Product> findByActiveTrueAndCategoryOrderByPositionAsc(ProductCategory category);

    Optional<Product> findByIdAndActiveTrue(Long id);

    @Query("select coalesce(max(p.position), 0) from Product p")
    int findMaxPosition();
}
