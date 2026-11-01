package com.ventanasyaluminios.repository;

import com.ventanasyaluminios.entity.Product;
import com.ventanasyaluminios.entity.enums.ProductCategory;
import java.util.List;
import java.util.Optional;

/** Acceso al catálogo de productos. */
public interface ProductRepository extends PositionedRepository<Product> {

    List<Product> findByActiveTrueOrderByPositionAsc();

    List<Product> findByActiveTrueAndCategoryOrderByPositionAsc(ProductCategory category);

    Optional<Product> findByIdAndActiveTrue(Long id);
}
