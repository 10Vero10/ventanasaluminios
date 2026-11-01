package com.ventanasyaluminios.controller;

import com.ventanasyaluminios.dto.response.GlassOptionsResponse;
import com.ventanasyaluminios.dto.response.ProductResponse;
import com.ventanasyaluminios.entity.enums.ProductCategory;
import com.ventanasyaluminios.service.GlassOptionService;
import com.ventanasyaluminios.service.ProductService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints públicos del catálogo y del configurador de vidrio. Solo muestran lo activo. */
@RestController
@RequestMapping("/api/v1")
public class CatalogController {

    private final ProductService productService;
    private final GlassOptionService glassOptionService;

    public CatalogController(ProductService productService, GlassOptionService glassOptionService) {
        this.productService = productService;
        this.glassOptionService = glassOptionService;
    }

    /** Sin {@code category} devuelve todo el catálogo activo. */
    @GetMapping("/products")
    public List<ProductResponse> products(@RequestParam(required = false) ProductCategory category) {
        return category == null ? productService.listPublic() : productService.listPublicByCategory(category);
    }

    @GetMapping("/products/{id}")
    public ProductResponse product(@PathVariable Long id) {
        return productService.getPublic(id);
    }

    @GetMapping("/glass-options")
    public GlassOptionsResponse glassOptions() {
        return glassOptionService.getActiveOptions();
    }
}
