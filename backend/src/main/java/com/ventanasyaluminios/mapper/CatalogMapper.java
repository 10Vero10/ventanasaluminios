package com.ventanasyaluminios.mapper;

import static com.ventanasyaluminios.mapper.TextNormalization.trimToNull;

import com.ventanasyaluminios.dto.request.GlassColorRequest;
import com.ventanasyaluminios.dto.request.GlassThicknessRequest;
import com.ventanasyaluminios.dto.request.GlassTypeRequest;
import com.ventanasyaluminios.dto.request.ProductRequest;
import com.ventanasyaluminios.dto.response.GlassColorResponse;
import com.ventanasyaluminios.dto.response.GlassThicknessResponse;
import com.ventanasyaluminios.dto.response.GlassTypeResponse;
import com.ventanasyaluminios.dto.response.ProductResponse;
import com.ventanasyaluminios.dto.response.ProductSpecResponse;
import com.ventanasyaluminios.entity.GlassColor;
import com.ventanasyaluminios.entity.GlassThickness;
import com.ventanasyaluminios.entity.GlassType;
import com.ventanasyaluminios.entity.Product;
import com.ventanasyaluminios.entity.ProductSpec;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Conversión entre entidades y DTO del módulo catalog (productos y opciones de vidrio). Los campos que
 * identifican una opción de vidrio (código, mm) no se copian aquí: los asigna el service solo al crear.
 */
@Component
public class CatalogMapper {

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getPosition(),
                product.getCategory(),
                product.getCategoryNumber(),
                product.getTitle(),
                product.getSubtitle(),
                product.getDescription(),
                product.getImageUrl(),
                product.getBadge(),
                product.getCtaText(),
                product.isActive(),
                product.getSpecs().stream().map(spec -> new ProductSpecResponse(spec.getLabel(), spec.getValue())).toList(),
                List.copyOf(product.getFeatures()));
    }

    /** Copia los campos editables y reemplaza ficha técnica y características en el orden recibido. */
    public void apply(ProductRequest request, Product product) {
        product.setCategory(request.category());
        product.setCategoryNumber(trimToNull(request.categoryNumber()));
        product.setTitle(request.title().trim());
        product.setSubtitle(request.subtitle().trim());
        product.setDescription(request.description().trim());
        product.setImageUrl(trimToNull(request.imageUrl()));
        product.setBadge(trimToNull(request.badge()));
        product.setCtaText(trimToNull(request.ctaText()));
        product.setActive(request.active());
        product.replaceSpecs(request.specs().stream()
                .map(spec -> new ProductSpec(spec.label().trim(), spec.value().trim()))
                .toList());
        product.replaceFeatures(request.features().stream().map(String::trim).toList());
    }

    /** Copia del producto para "Duplicar": mismo contenido, con otro título; posición y estado los decide el service. */
    public Product copyOf(Product source, String title) {
        Product copy = new Product();
        copy.setCategory(source.getCategory());
        copy.setCategoryNumber(source.getCategoryNumber());
        copy.setTitle(title);
        copy.setSubtitle(source.getSubtitle());
        copy.setDescription(source.getDescription());
        copy.setImageUrl(source.getImageUrl());
        copy.setBadge(source.getBadge());
        copy.setCtaText(source.getCtaText());
        copy.replaceSpecs(source.getSpecs().stream().map(spec -> new ProductSpec(spec.getLabel(), spec.getValue())).toList());
        copy.replaceFeatures(source.getFeatures());
        return copy;
    }

    public GlassTypeResponse toResponse(GlassType type) {
        return new GlassTypeResponse(type.getId(), type.getPosition(), type.getCode(), type.getName(), type.isActive(),
                type.isDefaultOption());
    }

    public void apply(GlassTypeRequest request, GlassType type) {
        type.setName(request.name().trim());
        type.setActive(request.active());
    }

    public GlassColorResponse toResponse(GlassColor color) {
        return new GlassColorResponse(color.getId(), color.getPosition(), color.getCode(), color.getName(),
                color.getHex(), color.isReflective(), color.getNote(), color.isActive(), color.isDefaultOption());
    }

    public void apply(GlassColorRequest request, GlassColor color) {
        color.setName(request.name().trim());
        color.setHex(request.hex().toUpperCase());
        color.setReflective(request.reflective());
        color.setNote(trimToNull(request.note()));
        color.setActive(request.active());
    }

    public GlassThicknessResponse toResponse(GlassThickness thickness) {
        return new GlassThicknessResponse(thickness.getId(), thickness.getPosition(), thickness.getMm(),
                thickness.isActive(), thickness.isDefaultOption());
    }

    public void apply(GlassThicknessRequest request, GlassThickness thickness) {
        thickness.setActive(request.active());
    }
}
