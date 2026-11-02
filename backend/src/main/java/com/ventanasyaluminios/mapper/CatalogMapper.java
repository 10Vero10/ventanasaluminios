package com.ventanasyaluminios.mapper;

import static com.ventanasyaluminios.mapper.TextNormalization.trimToNull;

import com.ventanasyaluminios.dto.request.GlassColorRequest;
import com.ventanasyaluminios.dto.request.GlassThicknessRequest;
import com.ventanasyaluminios.dto.request.GlassTypeRequest;
import com.ventanasyaluminios.dto.request.ProductRequest;
import com.ventanasyaluminios.dto.request.ProductSpecRequest;
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
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Conversión entre entidades y DTO del módulo catalog (productos y opciones de vidrio). El código o los mm de
 * una opción de vidrio no se copian aquí: van en su constructor y no cambian.
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
                product.getSpecs().stream()
                        .filter(Objects::nonNull)
                        .map(spec -> new ProductSpecResponse(spec.getLabel(), spec.getValue()))
                        .toList(),
                product.getFeatures().stream().filter(Objects::nonNull).toList());
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

    /**
     * Copia del producto para "Duplicar": mismo contenido, con otro título; posición, estado y fechas los decide el
     * service. Una fila nula (lista con huecos en la base) no se copia.
     */
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
        copy.replaceSpecs(source.getSpecs().stream().filter(Objects::nonNull).toList());
        copy.replaceFeatures(source.getFeatures().stream().filter(Objects::nonNull).toList());
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

    /** Producto actual como solicitud, para el respaldo. */
    public ProductRequest toRequest(ProductResponse p) {
        return new ProductRequest(p.category(), p.categoryNumber(), p.title(), p.subtitle(), p.description(),
                p.imageUrl(), p.badge(), p.ctaText(), p.active(),
                p.specs().stream().map(spec -> new ProductSpecRequest(spec.label(), spec.value())).toList(), p.features());
    }

    public GlassTypeRequest toRequest(GlassTypeResponse t) {
        return new GlassTypeRequest(t.code(), t.name(), t.active(), t.defaultOption());
    }

    public GlassColorRequest toRequest(GlassColorResponse c) {
        return new GlassColorRequest(c.code(), c.name(), c.hex(), c.reflective(), c.note(), c.active(), c.defaultOption());
    }

    public GlassThicknessRequest toRequest(GlassThicknessResponse t) {
        return new GlassThicknessRequest(t.mm(), t.active(), t.defaultOption());
    }

    /** Opción ausente en un respaldo: igual, pero inactiva y sin ser la de por defecto (no se borra). */
    public GlassTypeRequest toInactiveRequest(GlassTypeResponse t) {
        return new GlassTypeRequest(t.code(), t.name(), false, false);
    }

    public GlassColorRequest toInactiveRequest(GlassColorResponse c) {
        return new GlassColorRequest(c.code(), c.name(), c.hex(), c.reflective(), c.note(), false, false);
    }

    public GlassThicknessRequest toInactiveRequest(GlassThicknessResponse t) {
        return new GlassThicknessRequest(t.mm(), false, false);
    }
}
