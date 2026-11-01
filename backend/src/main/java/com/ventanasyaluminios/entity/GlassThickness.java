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

    public int getMm() {
        return mm;
    }

    public void setMm(int mm) {
        this.mm = mm;
    }
}
