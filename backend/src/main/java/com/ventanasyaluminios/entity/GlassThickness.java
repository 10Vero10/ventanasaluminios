package com.ventanasyaluminios.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Espesor de vidrio del configurador. Los milímetros son su identificador en las cotizaciones. */
@Entity
@Table(name = "glass_thickness")
public class GlassThickness extends GlassOption {

    @Column(nullable = false, updatable = false)
    private int mm;

    /** Para JPA. */
    protected GlassThickness() {
    }

    /** Los milímetros se fijan al crear y no cambian: las cotizaciones los guardan. */
    public GlassThickness(int mm) {
        this.mm = mm;
    }

    public int getMm() {
        return mm;
    }
}
