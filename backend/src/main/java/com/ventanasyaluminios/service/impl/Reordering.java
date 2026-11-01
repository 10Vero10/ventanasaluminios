package com.ventanasyaluminios.service.impl;

import com.ventanasyaluminios.exception.InvalidRequestException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/** Regla común al reordenar listas: el nuevo orden trae exactamente los ids actuales, sin repetir. */
final class Reordering {

    private Reordering() {
    }

    static void requireSameIds(Collection<Long> currentIds, List<Long> requestedIds) {
        var requested = new HashSet<>(requestedIds);
        if (requested.size() != requestedIds.size() || !requested.equals(new HashSet<>(currentIds))) {
            throw new InvalidRequestException("REORDER_MISMATCH",
                    "El nuevo orden no coincide con los elementos actuales. Actualiza la página e intenta de nuevo.");
        }
    }
}
