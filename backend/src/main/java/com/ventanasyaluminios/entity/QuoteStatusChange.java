package com.ventanasyaluminios.entity;

import com.ventanasyaluminios.entity.enums.QuoteStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Un cambio de estado de una cotización: el rastro que permite cambiar libremente sin perder la historia. */
@Entity
@Table(name = "quote_status_change")
public class QuoteStatusChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quote_id", nullable = false, updatable = false)
    private UUID quoteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", nullable = false, updatable = false)
    private QuoteStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, updatable = false)
    private QuoteStatus toStatus;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    /** Administrador que hizo el cambio; nulo hasta que exista el login (paso 4). */
    @Column(name = "changed_by", updatable = false)
    private Long changedBy;

    /** Para JPA. */
    protected QuoteStatusChange() {
    }

    public QuoteStatusChange(UUID quoteId, QuoteStatus fromStatus, QuoteStatus toStatus, Instant changedAt,
                             Long changedBy) {
        this.quoteId = quoteId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedAt = changedAt;
        this.changedBy = changedBy;
    }

    public Long getId() {
        return id;
    }

    public UUID getQuoteId() {
        return quoteId;
    }

    public QuoteStatus getFromStatus() {
        return fromStatus;
    }

    public QuoteStatus getToStatus() {
        return toStatus;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public Long getChangedBy() {
        return changedBy;
    }
}
