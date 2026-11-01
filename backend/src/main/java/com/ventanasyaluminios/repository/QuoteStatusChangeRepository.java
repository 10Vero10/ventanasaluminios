package com.ventanasyaluminios.repository;

import com.ventanasyaluminios.entity.QuoteStatusChange;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Historial de estados de las cotizaciones. */
public interface QuoteStatusChangeRepository extends JpaRepository<QuoteStatusChange, Long> {

    List<QuoteStatusChange> findByQuoteIdOrderByChangedAtAscIdAsc(UUID quoteId);
}
