package com.ventanasyaluminios.entity;

import com.ventanasyaluminios.entity.enums.QuoteStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * Solicitud de cotización enviada desde el sitio. Las eliminadas desde el panel conservan su fila con
 * {@code deleted_at}; {@link SQLRestriction} las oculta de toda consulta.
 */
@Entity
@Table(name = "quote")
@SQLRestriction("deleted_at IS NULL")
public class Quote {

    @Id
    private UUID id;

    @Column(name = "legacy_id", updatable = false)
    private String legacyId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(name = "product_type")
    private String productType;

    @Column(name = "dimensions_text")
    private String dimensionsText;

    private String message;

    @Column(name = "width_cm")
    private Integer widthCm;

    @Column(name = "height_cm")
    private Integer heightCm;

    private Integer panes;

    @Column(name = "area_m2")
    private BigDecimal areaM2;

    @Column(name = "glass_type_code")
    private String glassTypeCode;

    @Column(name = "glass_color_code")
    private String glassColorCode;

    @Column(name = "thickness_mm")
    private Integer thicknessMm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuoteStatus status;

    @Column(name = "source_ip", updatable = false)
    private String sourceIp;

    /** Para JPA. */
    protected Quote() {
    }

    /** Cotización nueva: estado NUEVA, fechas en {@code now} e IP de origen. */
    public Quote(UUID id, Instant now, String sourceIp) {
        this.id = id;
        this.createdAt = now;
        this.updatedAt = now;
        this.status = QuoteStatus.NUEVA;
        this.sourceIp = sourceIp;
    }

    /** Guarda los datos del configurador ya validados; van todos juntos o ninguno. */
    public void setConfiguration(Configuration configuration) {
        widthCm = configuration.widthCm();
        heightCm = configuration.heightCm();
        panes = configuration.panes();
        areaM2 = configuration.areaM2();
        glassTypeCode = configuration.glassTypeCode();
        glassColorCode = configuration.glassColorCode();
        thicknessMm = configuration.thicknessMm();
    }

    /** Datos del configurador de una cotización, ya validados y con el área calculada en el servidor. */
    public record Configuration(int widthCm, int heightCm, int panes, BigDecimal areaM2, String glassTypeCode,
                                String glassColorCode, int thicknessMm) {
    }

    /** Cambia el estado; devuelve el anterior. */
    public QuoteStatus changeStatus(QuoteStatus newStatus, Instant now) {
        QuoteStatus previous = status;
        status = newStatus;
        updatedAt = now;
        return previous;
    }

    /** Borrado lógico: la fila queda, pero ninguna consulta la vuelve a ver. */
    public void markDeleted(Instant now) {
        deletedAt = now;
        updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public String getLegacyId() {
        return legacyId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getDimensionsText() {
        return dimensionsText;
    }

    public void setDimensionsText(String dimensionsText) {
        this.dimensionsText = dimensionsText;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getWidthCm() {
        return widthCm;
    }

    public Integer getHeightCm() {
        return heightCm;
    }

    public Integer getPanes() {
        return panes;
    }

    public BigDecimal getAreaM2() {
        return areaM2;
    }

    public String getGlassTypeCode() {
        return glassTypeCode;
    }

    public String getGlassColorCode() {
        return glassColorCode;
    }

    public Integer getThicknessMm() {
        return thicknessMm;
    }

    public QuoteStatus getStatus() {
        return status;
    }

    public String getSourceIp() {
        return sourceIp;
    }
}
