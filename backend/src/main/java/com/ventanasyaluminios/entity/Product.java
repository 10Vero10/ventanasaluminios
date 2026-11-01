package com.ventanasyaluminios.entity;

import com.ventanasyaluminios.entity.enums.ProductCategory;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.hibernate.annotations.ListIndexBase;

/**
 * Producto del catálogo. La ficha técnica y las características son listas ordenadas que se guardan en sus
 * propias tablas; el orden de la lista es el orden en pantalla. Solo cambian con {@code replaceSpecs} y
 * {@code replaceFeatures}, y {@code version} impide que dos ediciones cruzadas mezclen sus listas.
 */
@Entity
@Table(name = "product")
public class Product implements Positioned {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;

    @Column(name = "category_number")
    private String categoryNumber;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String subtitle;

    @Column(nullable = false)
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    private String badge;

    @Column(name = "cta_text")
    private String ctaText;

    @Column(nullable = false)
    private boolean active;

    @Version
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ElementCollection
    @CollectionTable(name = "product_spec", joinColumns = @JoinColumn(name = "product_id"))
    @OrderColumn(name = "position")
    @ListIndexBase(1)
    private List<ProductSpec> specs = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "product_feature", joinColumns = @JoinColumn(name = "product_id"))
    @OrderColumn(name = "position")
    @ListIndexBase(1)
    @Column(name = "text", nullable = false)
    private List<String> features = new ArrayList<>();

    @Override
    public Long getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public void setPosition(int position) {
        this.position = position;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public String getCategoryNumber() {
        return categoryNumber;
    }

    public void setCategoryNumber(String categoryNumber) {
        this.categoryNumber = categoryNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getCtaText() {
        return ctaText;
    }

    public void setCtaText(String ctaText) {
        this.ctaText = ctaText;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ProductSpec> getSpecs() {
        return Collections.unmodifiableList(specs);
    }

    /** Reemplaza la ficha técnica conservando la colección que Hibernate ya administra. */
    public void replaceSpecs(List<ProductSpec> newSpecs) {
        List<ProductSpec> copy = List.copyOf(newSpecs);
        specs.clear();
        specs.addAll(copy);
    }

    public List<String> getFeatures() {
        return Collections.unmodifiableList(features);
    }

    /** Reemplaza las características conservando la colección que Hibernate ya administra. */
    public void replaceFeatures(List<String> newFeatures) {
        List<String> copy = List.copyOf(newFeatures);
        features.clear();
        features.addAll(copy);
    }
}
