package com.ventanasyaluminios.repository;

import com.ventanasyaluminios.entity.Quote;
import com.ventanasyaluminios.entity.enums.QuoteStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a las cotizaciones vigentes (las eliminadas quedan fuera por {@code @SQLRestriction}). */
public interface QuoteRepository extends JpaRepository<Quote, UUID> {

    Page<Quote> findByStatus(QuoteStatus status, Pageable pageable);

    long countByStatus(QuoteStatus status);
}
