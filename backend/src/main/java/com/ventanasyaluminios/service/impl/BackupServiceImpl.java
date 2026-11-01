package com.ventanasyaluminios.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ventanasyaluminios.dto.request.BackupRequest;
import com.ventanasyaluminios.dto.request.FeaturedLineRequest;
import com.ventanasyaluminios.dto.request.GalleryImageRequest;
import com.ventanasyaluminios.dto.request.GlassColorRequest;
import com.ventanasyaluminios.dto.request.GlassThicknessRequest;
import com.ventanasyaluminios.dto.request.GlassTypeRequest;
import com.ventanasyaluminios.dto.request.ProductRequest;
import com.ventanasyaluminios.dto.request.SiteSettingsRequest;
import com.ventanasyaluminios.dto.response.BackupImportResponse;
import com.ventanasyaluminios.dto.response.GlassColorResponse;
import com.ventanasyaluminios.dto.response.GlassOptionsResponse;
import com.ventanasyaluminios.dto.response.GlassThicknessResponse;
import com.ventanasyaluminios.dto.response.GlassTypeResponse;
import com.ventanasyaluminios.exception.ApiException;
import com.ventanasyaluminios.exception.ErrorCode;
import com.ventanasyaluminios.mapper.CatalogMapper;
import com.ventanasyaluminios.mapper.SettingsMapper;
import com.ventanasyaluminios.repository.FeaturedLineRepository;
import com.ventanasyaluminios.repository.GalleryImageRepository;
import com.ventanasyaluminios.repository.ProductRepository;
import com.ventanasyaluminios.service.BackupService;
import com.ventanasyaluminios.service.FeaturedLineService;
import com.ventanasyaluminios.service.GalleryService;
import com.ventanasyaluminios.service.GlassOptionService;
import com.ventanasyaluminios.service.ProductService;
import com.ventanasyaluminios.service.SiteSettingsService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link BackupService}. Reutiliza los services de cada módulo para que el respaldo cumpla las
 * mismas reglas que el panel (WhatsApp, opción por defecto, códigos que no cambian, posiciones).
 */
@Service
public class BackupServiceImpl implements BackupService {

    private final SiteSettingsService settingsService;
    private final FeaturedLineService featuredLineService;
    private final GalleryService galleryService;
    private final ProductService productService;
    private final GlassOptionService glassOptionService;
    private final FeaturedLineRepository featuredLineRepository;
    private final GalleryImageRepository galleryImageRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final Clock clock;
    private final SettingsMapper settingsMapper;
    private final CatalogMapper catalogMapper;

    public BackupServiceImpl(SiteSettingsService settingsService, FeaturedLineService featuredLineService,
                             GalleryService galleryService, ProductService productService,
                             GlassOptionService glassOptionService, FeaturedLineRepository featuredLineRepository,
                             GalleryImageRepository galleryImageRepository, ProductRepository productRepository,
                             ObjectMapper objectMapper, Validator validator, Clock clock,
                             SettingsMapper settingsMapper, CatalogMapper catalogMapper) {
        this.settingsService = settingsService;
        this.featuredLineService = featuredLineService;
        this.galleryService = galleryService;
        this.productService = productService;
        this.glassOptionService = glassOptionService;
        this.featuredLineRepository = featuredLineRepository;
        this.galleryImageRepository = galleryImageRepository;
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.clock = clock;
        this.settingsMapper = settingsMapper;
        this.catalogMapper = catalogMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public BackupRequest export() {
        SiteSettingsRequest settings = settingsMapper.toRequest(settingsService.getSettings());
        List<FeaturedLineRequest> lines = featuredLineService.list().stream().map(settingsMapper::toRequest).toList();
        List<GalleryImageRequest> gallery = galleryService.list().stream().map(settingsMapper::toRequest).toList();
        GlassOptionsResponse glass = glassOptionService.getAllOptions();
        var glassOptions = new BackupRequest.GlassOptions(
                glass.types().stream().map(catalogMapper::toRequest).toList(),
                glass.colors().stream().map(catalogMapper::toRequest).toList(),
                glass.thicknesses().stream().map(catalogMapper::toRequest).toList());
        List<ProductRequest> products = productService.list().stream().map(catalogMapper::toRequest).toList();
        return new BackupRequest(BackupRequest.CURRENT_VERSION, Instant.now(clock),
                new BackupRequest.Content(settings, lines, gallery, glassOptions), products);
    }

    @Override
    @Transactional
    public BackupImportResponse importBackup(String json) {
        BackupRequest backup = parse(json);
        settingsService.updateSettings(settingsToImport(backup, json));

        featuredLineRepository.deleteAllInBatch();
        backup.content().featuredLines().forEach(featuredLineService::create);

        galleryImageRepository.deleteAllInBatch();
        backup.content().gallery().forEach(galleryService::create);

        // Ficha y características se borran en cascada en la base al borrar cada producto.
        productRepository.deleteAll(productRepository.findAll());
        productRepository.flush();
        backup.products().forEach(productService::create);

        importGlassOptions(backup.content().glassOptions());
        return new BackupImportResponse(backup.content().featuredLines().size(), backup.content().gallery().size(),
                backup.products().size(), backup.content().glassOptions().types().size(),
                backup.content().glassOptions().colors().size(), backup.content().glassOptions().thicknesses().size());
    }

    /**
     * Configuración a guardar. Un respaldo anterior a la foto del taller (V7) no trae {@code aboutImageUrl}: en ese caso
     * se conserva la foto actual en vez de borrarla. Si el campo viene, aunque sea null, se respeta.
     */
    private SiteSettingsRequest settingsToImport(BackupRequest backup, String json) {
        SiteSettingsRequest settings = backup.content().settings();
        if (hasSettingsField(json, "aboutImageUrl")) {
            return settings;
        }
        return settings.withAboutImageUrl(settingsService.getSettings().aboutImageUrl());
    }

    private boolean hasSettingsField(String json, String field) {
        try {
            return objectMapper.readTree(json).path("content").path("settings").has(field);
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /** Valida el archivo con los mensajes del briefing §3.10 antes de tocar nada. */
    private BackupRequest parse(String json) {
        JsonNode root;
        try {
            root = objectMapper.readTree(json == null ? "" : json);
        } catch (JsonProcessingException e) {
            throw invalid("El archivo no es un JSON válido.");
        }
        if (root == null || !root.isObject()) {
            throw invalid("El archivo no es un JSON válido.");
        }
        if (!root.hasNonNull("content")) {
            throw invalid("El archivo no parece un respaldo del sitio (falta \"content\").");
        }
        if (!root.path("products").isArray()) {
            throw invalid("El respaldo tiene un catálogo inválido (\"products\" debe ser una lista).");
        }
        if (root.path("version").asInt(-1) != BackupRequest.CURRENT_VERSION) {
            throw invalid("Esta versión de respaldo no es compatible con el sitio.");
        }
        BackupRequest backup;
        try {
            backup = objectMapper.treeToValue(root, BackupRequest.class);
        } catch (JsonProcessingException e) {
            throw invalid("El respaldo tiene datos con un formato inválido.");
        }
        validator.validate(backup).stream()
                .min(Comparator.comparing((ConstraintViolation<BackupRequest> v) -> v.getPropertyPath().toString()))
                .ifPresent(violation -> {
                    throw invalid("El respaldo no es válido: " + violation.getMessage());
                });
        return backup;
    }

    /**
     * Actualiza por código las opciones que trae el respaldo, crea las nuevas y desactiva las ausentes (nunca las
     * borra: las cotizaciones guardan sus códigos). El orden queda el del respaldo, con las ausentes al final.
     */
    private void importGlassOptions(BackupRequest.GlassOptions options) {
        GlassOptionsResponse current = glassOptionService.getAllOptions();

        var types = current.types().stream().collect(Collectors.toMap(GlassTypeResponse::code, Function.identity()));
        List<Long> typeOrder = new ArrayList<>();
        for (GlassTypeRequest request : options.types()) {
            var existing = types.remove(request.code());
            typeOrder.add(existing == null ? glassOptionService.createType(request).id()
                    : glassOptionService.updateType(existing.id(), request).id());
        }
        types.values().forEach(absent -> typeOrder.add(glassOptionService.updateType(absent.id(),
                new GlassTypeRequest(absent.code(), absent.name(), false, false)).id()));
        glassOptionService.reorderTypes(typeOrder);

        var colors = current.colors().stream().collect(Collectors.toMap(GlassColorResponse::code, Function.identity()));
        List<Long> colorOrder = new ArrayList<>();
        for (GlassColorRequest request : options.colors()) {
            var existing = colors.remove(request.code());
            colorOrder.add(existing == null ? glassOptionService.createColor(request).id()
                    : glassOptionService.updateColor(existing.id(), request).id());
        }
        colors.values().forEach(absent -> colorOrder.add(glassOptionService.updateColor(absent.id(),
                new GlassColorRequest(absent.code(), absent.name(), absent.hex(), absent.reflective(), absent.note(),
                        false, false)).id()));
        glassOptionService.reorderColors(colorOrder);

        Map<Integer, GlassThicknessResponse> thicknesses = current.thicknesses()
                .stream().collect(Collectors.toMap(GlassThicknessResponse::mm, Function.identity()));
        List<Long> thicknessOrder = new ArrayList<>();
        for (GlassThicknessRequest request : options.thicknesses()) {
            var existing = thicknesses.remove(request.mm());
            thicknessOrder.add(existing == null ? glassOptionService.createThickness(request).id()
                    : glassOptionService.updateThickness(existing.id(), request).id());
        }
        thicknesses.values().forEach(absent -> thicknessOrder.add(glassOptionService.updateThickness(absent.id(),
                new GlassThicknessRequest(absent.mm(), false, false)).id()));
        glassOptionService.reorderThicknesses(thicknessOrder);
    }

    private static ApiException invalid(String message) {
        return new ApiException(ErrorCode.INVALID_BACKUP, message);
    }
}
