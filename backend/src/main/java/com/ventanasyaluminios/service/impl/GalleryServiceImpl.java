package com.ventanasyaluminios.service.impl;

import com.ventanasyaluminios.dto.request.GalleryImageRequest;
import com.ventanasyaluminios.dto.response.GalleryImageResponse;
import com.ventanasyaluminios.entity.GalleryImage;
import com.ventanasyaluminios.entity.enums.ProductCategory;
import com.ventanasyaluminios.exception.ResourceNotFoundException;
import com.ventanasyaluminios.mapper.SettingsMapper;
import com.ventanasyaluminios.repository.GalleryImageRepository;
import com.ventanasyaluminios.service.GalleryService;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de {@link GalleryService}. */
@Service
public class GalleryServiceImpl implements GalleryService {

    private final GalleryImageRepository repository;
    private final SettingsMapper mapper;
    private final Clock clock;

    public GalleryServiceImpl(GalleryImageRepository repository, SettingsMapper mapper, Clock clock) {
        this.repository = repository;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GalleryImageResponse> list() {
        return toResponses(repository.findAllByOrderByPositionAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GalleryImageResponse> listByCategory(ProductCategory category) {
        return toResponses(repository.findByCategoryOrderByPositionAsc(category));
    }

    @Override
    @Transactional
    public GalleryImageResponse create(GalleryImageRequest request) {
        GalleryImage image = new GalleryImage();
        mapper.apply(request, image);
        image.setPosition(repository.findMaxPosition() + 1);
        image.setCreatedAt(Instant.now(clock));
        return mapper.toResponse(repository.save(image));
    }

    @Override
    @Transactional
    public GalleryImageResponse update(Long id, GalleryImageRequest request) {
        GalleryImage image = find(id);
        mapper.apply(request, image);
        return mapper.toResponse(image);
    }

    /**
     * Borra la imagen de la galería pero no el archivo de {@code /media}: la misma imagen puede seguir en
     * uso como portada o en una línea destacada.
     */
    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    @Override
    @Transactional
    public List<GalleryImageResponse> reorder(List<Long> ids) {
        return toResponses(Reordering.apply(repository.findAll(), ids));
    }

    private List<GalleryImageResponse> toResponses(List<GalleryImage> images) {
        return images.stream().map(mapper::toResponse).toList();
    }

    private GalleryImage find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La imagen no existe."));
    }
}
