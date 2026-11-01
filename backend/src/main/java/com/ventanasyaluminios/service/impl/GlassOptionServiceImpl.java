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
import com.ventanasyaluminios.repository.GlassThicknessRepository;
import com.ventanasyaluminios.repository.GlassTypeRepository;
import com.ventanasyaluminios.service.GlassOptionService;
import java.util.List;
import java.util.Objects;
import org.springframework.data.jpa.repository.JpaRepository;
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
        GlassType type = new GlassType();
        type.setCode(request.code());
        mapper.apply(request, type);
        type.setPosition(typeRepository.findMaxPosition() + 1);
        applyDefault(type, request.defaultOption(), typeRepository);
        return mapper.toResponse(typeRepository.save(type));
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
        GlassColor color = new GlassColor();
        color.setCode(request.code());
        mapper.apply(request, color);
        color.setPosition(colorRepository.findMaxPosition() + 1);
        applyDefault(color, request.defaultOption(), colorRepository);
        return mapper.toResponse(colorRepository.save(color));
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
        GlassThickness thickness = new GlassThickness();
        thickness.setMm(request.mm());
        mapper.apply(request, thickness);
        thickness.setPosition(thicknessRepository.findMaxPosition() + 1);
        applyDefault(thickness, request.defaultOption(), thicknessRepository);
        return mapper.toResponse(thicknessRepository.save(thickness));
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

    /**
     * Marca o desmarca {@code option} como opción por defecto de su lista. La anterior se desmarca y se escribe
     * primero, porque el índice único parcial de la base no admite dos marcadas ni por un instante.
     */
    private static <T extends GlassOption> void applyDefault(T option, boolean requested, JpaRepository<T, Long> repository) {
        if (!requested) {
            option.setDefaultOption(false);
            return;
        }
        if (!option.isActive()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "La opción por defecto debe estar activa.");
        }
        if (option.isDefaultOption()) {
            return;
        }
        repository.findAll().stream()
                .filter(other -> other != option && other.isDefaultOption())
                .forEach(other -> other.setDefaultOption(false));
        repository.flush();
        option.setDefaultOption(true);
    }

    private static void requireUnchanged(Object current, Object requested, String field) {
        if (!Objects.equals(current, requested)) {
            throw new ApiException(ErrorCode.IMMUTABLE_FIELD, IMMUTABLE_MESSAGE.formatted(field));
        }
    }

    private static <T> T find(JpaRepository<T, Long> repository, Long id, String message) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(message));
    }

    private static ApiException duplicate(String message) {
        return new ApiException(ErrorCode.DUPLICATE_CODE, message);
    }
}
