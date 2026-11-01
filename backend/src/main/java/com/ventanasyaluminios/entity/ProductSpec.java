package com.ventanasyaluminios.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Fila de la ficha técnica de un producto ("Hojas: 2, 3 o 4 hojas deslizantes"). */
@Embeddable
public class ProductSpec {

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String value;

    protected ProductSpec() {
    }

    public ProductSpec(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }
}
