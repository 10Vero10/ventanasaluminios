package com.ventanasyaluminios.service;

import com.ventanasyaluminios.dto.request.GlassColorRequest;
import com.ventanasyaluminios.dto.request.GlassThicknessRequest;
import com.ventanasyaluminios.dto.request.GlassTypeRequest;
import com.ventanasyaluminios.dto.response.GlassColorResponse;
import com.ventanasyaluminios.dto.response.GlassOptionsResponse;
import com.ventanasyaluminios.dto.response.GlassThicknessResponse;
import com.ventanasyaluminios.dto.response.GlassTypeResponse;
import java.util.List;

/**
 * Opciones del configurador de vidrio. No se borran: se desactivan. Su código (o los mm) no cambia después de
 * crearse. Como máximo una opción por defecto por lista, y debe estar activa.
 */
public interface GlassOptionService {

    /** Solo las opciones activas, para el configurador público. */
    GlassOptionsResponse getActiveOptions();

    /** Todas las opciones, activas e inactivas, para el panel. */
    GlassOptionsResponse getAllOptions();

    GlassTypeResponse createType(GlassTypeRequest request);

    GlassTypeResponse updateType(Long id, GlassTypeRequest request);

    List<GlassTypeResponse> reorderTypes(List<Long> ids);

    GlassColorResponse createColor(GlassColorRequest request);

    GlassColorResponse updateColor(Long id, GlassColorRequest request);

    List<GlassColorResponse> reorderColors(List<Long> ids);

    GlassThicknessResponse createThickness(GlassThicknessRequest request);

    GlassThicknessResponse updateThickness(Long id, GlassThicknessRequest request);

    List<GlassThicknessResponse> reorderThicknesses(List<Long> ids);
}
