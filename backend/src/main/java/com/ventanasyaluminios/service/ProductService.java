package com.ventanasyaluminios.service;

import com.ventanasyaluminios.dto.request.ProductRequest;
import com.ventanasyaluminios.dto.response.ProductResponse;
import com.ventanasyaluminios.entity.enums.ProductCategory;
import java.util.List;

/** Catálogo de productos: lo que ve el visitante (solo activos) y la administración completa. */
public interface ProductService {

    /** Productos activos en orden. */
    List<ProductResponse> listPublic();

    /** Productos activos de una línea, en orden. */
    List<ProductResponse> listPublicByCategory(ProductCategory category);

    /** Detalle de un producto activo; uno oculto responde como inexistente. */
    ProductResponse getPublic(Long id);

    /** Todos los productos, activos y ocultos, en orden. */
    List<ProductResponse> list();

    ProductResponse get(Long id);

    /** Crea el producto al final del catálogo. */
    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    /** Copia el producto con el título "… (copia)", al final y oculto hasta que se edite. */
    ProductResponse duplicate(Long id);

    void delete(Long id);

    /** Aplica el orden recibido; debe incluir todos los ids existentes. */
    List<ProductResponse> reorder(List<Long> ids);
}
