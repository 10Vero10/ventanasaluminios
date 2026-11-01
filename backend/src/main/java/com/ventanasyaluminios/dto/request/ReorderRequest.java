package com.ventanasyaluminios.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Nuevo orden de una lista. Debe traer exactamente los ids existentes, cada uno una vez.
 *
 * @param ids ids en el orden deseado
 */
public record ReorderRequest(
        @NotEmpty(message = "La lista de orden no puede estar vacía.")
        List<@NotNull(message = "La lista de orden tiene un id vacío.") Long> ids) {
}
