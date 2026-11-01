package com.ventanasyaluminios.entity;

import com.ventanasyaluminios.entity.enums.ProductCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Tarjeta de la portada que lleva al catálogo filtrado por una línea de producto. */
@Entity
@Table(name = "featured_line")
public class FeaturedLine implements Positioned {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private String number;

    @Column(nullable = false)
    private String title;

    @Column(name = "category_tag", nullable = false)
    private String categoryTag;

    @Column(nullable = false)
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "link_text", nullable = false)
    private String linkText;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_category", nullable = false)
    private ProductCategory targetCategory;

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

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategoryTag() {
        return categoryTag;
    }

    public void setCategoryTag(String categoryTag) {
        this.categoryTag = categoryTag;
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

    public String getLinkText() {
        return linkText;
    }

    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }

    public ProductCategory getTargetCategory() {
        return targetCategory;
    }

    public void setTargetCategory(ProductCategory targetCategory) {
        this.targetCategory = targetCategory;
    }
}
