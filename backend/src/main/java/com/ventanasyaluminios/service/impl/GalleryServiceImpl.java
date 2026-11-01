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
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de {@link GalleryService}. */
@Service
public class GalleryServiceImpl implements GalleryService {

    /** Título que se usa cuando el administrador no escribe uno (briefing §3.3). */
    static final String DEFAULT_TITLE = "Imagen";

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
    public List<GalleryImageResponse> list(ProductCategory category) {
        List<GalleryImage> images = category == null
                ? repository.findAllByOrderByPositionAsc()
                : repository.findByCategoryOrderByPositionAsc(category);
        return images.stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public GalleryImageResponse create(GalleryImageRequest request) {
        GalleryImage image = new GalleryImage();
        apply(request, image);
        image.setPosition(repository.findMaxPosition() + 1);
        image.setCreatedAt(Instant.now(clock));
        return mapper.toResponse(repository.save(image));
    }

    @Override
    @Transactional
    public GalleryImageResponse update(Long id, GalleryImageRequest request) {
        GalleryImage image = find(id);
        apply(request, image);
        return mapper.toResponse(image);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    @Override
    @Transactional
    public List<GalleryImageResponse> reorder(List<Long> ids) {
        var byId = repository.findAll().stream().collect(Collectors.toMap(GalleryImage::getId, Function.identity()));
        Reordering.requireSameIds(byId.keySet(), ids);
        for (int i = 0; i < ids.size(); i++) {
            byId.get(ids.get(i)).setPosition(i + 1);
        }
        repository.flush();
        return list(null);
    }

    private void apply(GalleryImageRequest request, GalleryImage image) {
        String title = request.title() == null || request.title().isBlank() ? DEFAULT_TITLE : request.title().trim();
        image.setTitle(title);
        image.setUrl(request.url().trim());
        image.setCategory(request.category());
    }

    private GalleryImage find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La imagen no existe."));
    }
}
