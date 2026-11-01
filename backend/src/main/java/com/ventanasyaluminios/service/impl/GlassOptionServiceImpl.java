package com.ventanasyaluminios.service.impl;

import com.ventanasyaluminios.dto.request.GlassColorRequest;
import com.ventanasyaluminios.dto.request.GlassThicknessRequest;
import com.ventanasyaluminios.dto.request.GlassTypeRequest;
import com.ventanasyaluminios.dto.response.GlassColorResponse;
import com.ventanasyaluminios.dto.response.GlassOptionsResponse;
import com.ventanasyaluminios.dto.response.GlassThicknessResponse;
import com.ventanasyaluminios.dto.response.GlassTypeResponse;
import com.ventanasyaluminios.entity.GlassColor;
import com.ventanasyaluminios.entity.GlassOption;
import com.ventanasyaluminios.entity.GlassThickness;
import com.ventanasyaluminios.entity.GlassType;
import com.ventanasyaluminios.exception.ApiException;
import com.ventanasyaluminios.exception.ErrorCode;
import com.ventanasyaluminios.exception.ResourceNotFoundException;
import com.ventanasyaluminios.mapper.CatalogMapper;
import com.ventanasyaluminios.repository.GlassColorRepository;
import com.ventanasyaluminios.repository.GlassOptionRepository;
import com.ventanasyaluminios.repository.GlassThicknessRepository;
import com.ventanasyaluminios.repository.GlassTypeRepository;
import com.ventanasyaluminios.service.GlassOptionService;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de {@link GlassOptionService}. */
@Service
public class GlassOptionServiceImpl implements GlassOptionService {

    private static final String IMMUTABLE_MESSAGE = "%s no se puede cambiar porque las cotizaciones guardadas lo usan."
            + " Crea una opción nueva y desactiva esta.";

    private final GlassTypeRepository typeRepository;
    private final GlassColorRepository colorRepository;
    private final GlassThicknessRepository thicknessRepository;
    private final CatalogMapper mapper;

    public GlassOptionServiceImpl(GlassTypeRepository typeRepository,
                                  GlassColorRepository colorRepository,
                                  GlassThicknessRepository thicknessRepository,
                                  CatalogMapper mapper) {
        this.typeRepository = typeRepository;
        this.colorRepository = colorRepository;
        this.thicknessRepository = thicknessRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public GlassOptionsResponse getActiveOptions() {
        return new GlassOptionsResponse(
                typeRepository.findByActiveTrueOrderByPositionAsc().stream().map(mapper::toResponse).toList(),
                colorRepository.findByActiveTrueOrderByPositionAsc().stream().map(mapper::toResponse).toList(),
                thicknessRepository.findByActiveTrueOrderByPositionAsc().stream().map(mapper::toResponse).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GlassOptionsResponse getAllOptions() {
        return new GlassOptionsResponse(
                typeRepository.findAllByOrderByPositionAsc().stream().map(mapper::toResponse).toList(),
                colorRepository.findAllByOrderByPositionAsc().stream().map(mapper::toResponse).toList(),
                thicknessRepository.findAllByOrderByPositionAsc().stream().map(mapper::toResponse).toList());
    }

    @Override
    @Transactional
    public GlassTypeResponse createType(GlassTypeRequest request) {
        if (typeRepository.existsByCode(request.code())) {
            throw duplicate("Ya existe un tipo de vidrio con el código " + request.code() + ".");
        }
        GlassType type = new GlassType(request.code());
        mapper.apply(request, type);
        return mapper.toResponse(saveAtEnd(type, request.defaultOption(), typeRepository));
    }

    @Override
    @Transactional
    public GlassTypeResponse updateType(Long id, GlassTypeRequest request) {
        GlassType type = find(typeRepository, id, "El tipo de vidrio no existe.");
        requireUnchanged(type.getCode(), request.code(), "El código");
        mapper.apply(request, type);
        applyDefault(type, request.defaultOption(), typeRepository);
        return mapper.toResponse(type);
    }

    @Override
    @Transactional
    public List<GlassTypeResponse> reorderTypes(List<Long> ids) {
        return Reordering.apply(typeRepository.findAll(), ids).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public GlassColorResponse createColor(GlassColorRequest request) {
        if (colorRepository.existsByCode(request.code())) {
            throw duplicate("Ya existe un color de vidrio con el código " + request.code() + ".");
        }
        GlassColor color = new GlassColor(request.code());
        mapper.apply(request, color);
        return mapper.toResponse(saveAtEnd(color, request.defaultOption(), colorRepository));
    }

    @Override
    @Transactional
    public GlassColorResponse updateColor(Long id, GlassColorRequest request) {
        GlassColor color = find(colorRepository, id, "El color de vidrio no existe.");
        requireUnchanged(color.getCode(), request.code(), "El código");
        mapper.apply(request, color);
        applyDefault(color, request.defaultOption(), colorRepository);
        return mapper.toResponse(color);
    }

    @Override
    @Transactional
    public List<GlassColorResponse> reorderColors(List<Long> ids) {
        return Reordering.apply(colorRepository.findAll(), ids).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public GlassThicknessResponse createThickness(GlassThicknessRequest request) {
        if (thicknessRepository.existsByMm(request.mm())) {
            throw duplicate("Ya existe el espesor de " + request.mm() + " mm.");
        }
        GlassThickness thickness = new GlassThickness(request.mm());
        mapper.apply(request, thickness);
        return mapper.toResponse(saveAtEnd(thickness, request.defaultOption(), thicknessRepository));
    }

    @Override
    @Transactional
    public GlassThicknessResponse updateThickness(Long id, GlassThicknessRequest request) {
        GlassThickness thickness = find(thicknessRepository, id, "El espesor no existe.");
        requireUnchanged(thickness.getMm(), request.mm(), "El espesor");
        mapper.apply(request, thickness);
        applyDefault(thickness, request.defaultOption(), thicknessRepository);
        return mapper.toResponse(thickness);
    }

    @Override
    @Transactional
    public List<GlassThicknessResponse> reorderThicknesses(List<Long> ids) {
        return Reordering.apply(thicknessRepository.findAll(), ids).stream().map(mapper::toResponse).toList();
    }

    /** Guarda una opción nueva al final de su lista. */
    private static <T extends GlassOption> T saveAtEnd(T option, boolean isDefault, GlassOptionRepository<T> repository) {
        option.setPosition(repository.findMaxPosition() + 1);
        applyDefault(option, isDefault, repository);
        return repository.save(option);
    }

    /**
     * Marca o desmarca {@code option} como opción por defecto. Si se marca, la anterior se desmarca; la restricción
     * "una sola por lista" se verifica al confirmar, así que el orden de escritura no importa.
     */
    private static <T extends GlassOption> void applyDefault(T option, boolean requested, GlassOptionRepository<T> repository) {
        if (requested && !option.isActive()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "La opción por defecto debe estar activa.");
        }
        if (requested) {
            repository.findByDefaultOptionTrue()
                    .filter(previous -> previous != option)
                    .ifPresent(previous -> previous.setDefaultOption(false));
        }
        option.setDefaultOption(requested);
    }

    private static void requireUnchanged(Object current, Object requested, String field) {
        if (!Objects.equals(current, requested)) {
            throw new ApiException(ErrorCode.IMMUTABLE_FIELD, IMMUTABLE_MESSAGE.formatted(field));
        }
    }

    private static <T extends GlassOption> T find(GlassOptionRepository<T> repository, Long id, String message) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(message));
    }

    private static ApiException duplicate(String message) {
        return new ApiException(ErrorCode.DUPLICATE_CODE, message);
    }
}
