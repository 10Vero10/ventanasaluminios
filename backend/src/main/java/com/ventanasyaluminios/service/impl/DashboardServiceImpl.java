package com.ventanasyaluminios.service.impl;

import com.ventanasyaluminios.dto.response.DashboardResponse;
import com.ventanasyaluminios.entity.enums.QuoteStatus;
import com.ventanasyaluminios.repository.GalleryImageRepository;
import com.ventanasyaluminios.repository.ProductRepository;
import com.ventanasyaluminios.repository.QuoteRepository;
import com.ventanasyaluminios.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de {@link DashboardService}. */
@Service
public class DashboardServiceImpl implements DashboardService {

    private final ProductRepository productRepository;
    private final GalleryImageRepository galleryImageRepository;
    private final QuoteRepository quoteRepository;

    public DashboardServiceImpl(ProductRepository productRepository, GalleryImageRepository galleryImageRepository,
                                QuoteRepository quoteRepository) {
        this.productRepository = productRepository;
        this.galleryImageRepository = galleryImageRepository;
        this.quoteRepository = quoteRepository;
    }

    /** Productos del catálogo (también los ocultos), imágenes de la galería y cotizaciones sin atender. */
    @Override
    @Transactional(readOnly = true)
    public DashboardResponse get() {
        return new DashboardResponse(productRepository.count(), galleryImageRepository.count(),
                quoteRepository.countByStatus(QuoteStatus.NUEVA));
    }
}
