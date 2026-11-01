package com.ventanasyaluminios.service.impl;

import com.ventanasyaluminios.dto.request.ProductRequest;
import com.ventanasyaluminios.dto.response.ProductResponse;
import com.ventanasyaluminios.entity.Product;
import com.ventanasyaluminios.entity.enums.ProductCategory;
import com.ventanasyaluminios.exception.ResourceNotFoundException;
import com.ventanasyaluminios.mapper.CatalogMapper;
import com.ventanasyaluminios.repository.ProductRepository;
import com.ventanasyaluminios.service.ProductService;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de {@link ProductService}. */
@Service
public class ProductServiceImpl implements ProductService {

    static final String COPY_SUFFIX = " (copia)";

    /** Largo máximo de product.title (V3). */
    static final int MAX_TITLE_LENGTH = 80;

    private final ProductRepository repository;
    private final CatalogMapper mapper;
    private final Clock clock;

    public ProductServiceImpl(ProductRepository repository, CatalogMapper mapper, Clock clock) {
        this.repository = repository;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> listPublic() {
        return toResponses(repository.findByActiveTrueOrderByPositionAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> listPublicByCategory(ProductCategory category) {
        return toResponses(repository.findByActiveTrueAndCategoryOrderByPositionAsc(category));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getPublic(Long id) {
        return mapper.toResponse(repository.findByIdAndActiveTrue(id).orElseThrow(ProductServiceImpl::notFound));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> list() {
        return toResponses(repository.findAllByOrderByPositionAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        mapper.apply(request, product);
        return mapper.toResponse(saveAtEnd(product));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = find(id);
        mapper.apply(request, product);
        product.setUpdatedAt(Instant.now(clock));
        return mapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse duplicate(Long id) {
        Product source = find(id);
        Product copy = mapper.copyOf(source, copyTitle(source.getTitle()));
        copy.setActive(false);
        return mapper.toResponse(saveAtEnd(copy));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    @Override
    @Transactional
    public List<ProductResponse> reorder(List<Long> ids) {
        return toResponses(Reordering.apply(repository.findAll(), ids));
    }

    /** "Corredizas" pasa a "Corredizas (copia)", recortando el título si no cabe en 80 caracteres. */
    static String copyTitle(String title) {
        int room = MAX_TITLE_LENGTH - COPY_SUFFIX.length();
        String base = title.length() > room ? title.substring(0, room).stripTrailing() : title;
        return base + COPY_SUFFIX;
    }

    private Product saveAtEnd(Product product) {
        Instant now = Instant.now(clock);
        product.setPosition(repository.findMaxPosition() + 1);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        return repository.save(product);
    }

    private List<ProductResponse> toResponses(List<Product> products) {
        return products.stream().map(mapper::toResponse).toList();
    }

    private Product find(Long id) {
        return repository.findById(id).orElseThrow(ProductServiceImpl::notFound);
    }

    private static ResourceNotFoundException notFound() {
        return new ResourceNotFoundException("El producto no existe.");
    }
}
