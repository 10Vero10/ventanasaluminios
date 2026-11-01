package com.ventanasyaluminios.service.impl;

import com.ventanasyaluminios.entity.Positioned;
import com.ventanasyaluminios.exception.ApiException;
import com.ventanasyaluminios.exception.ErrorCode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reordenamiento común a toda lista ordenable (líneas destacadas, galería y, en el catálogo, productos,
 * especificaciones y opciones de vidrio).
 */
final class Reordering {

    private Reordering() {
    }

    /**
     * Asigna posiciones 1..n según {@code ids}, que debe traer exactamente los ids de {@code current}, cada
     * uno una vez. Así no se pierde un elemento agregado mientras el administrador ordenaba en otra pestaña.
     *
     * @return los elementos en el nuevo orden
     */
    static <T extends Positioned> List<T> apply(List<T> current, List<Long> ids) {
        Map<Long, T> byId = current.stream().collect(Collectors.toMap(Positioned::getId, Function.identity()));
        if (ids.size() != byId.size() || !new HashSet<>(ids).equals(byId.keySet())) {
            throw new ApiException(ErrorCode.REORDER_MISMATCH,
                    "El nuevo orden no coincide con los elementos actuales. Actualiza la página e intenta de nuevo.");
        }
        List<T> ordered = ids.stream().map(byId::get).toList();
        for (int i = 0; i < ordered.size(); i++) {
            ordered.get(i).setPosition(i + 1);
        }
        return ordered;
    }
}
