package com.ventanasyaluminios.service;

import com.ventanasyaluminios.dto.request.FeaturedLineRequest;
import com.ventanasyaluminios.dto.response.FeaturedLineResponse;
import java.util.List;

/** Líneas destacadas de la portada. */
public interface FeaturedLineService {

    List<FeaturedLineResponse> list();

    /** Crea la línea al final de la lista. */
    FeaturedLineResponse create(FeaturedLineRequest request);

    FeaturedLineResponse update(Long id, FeaturedLineRequest request);

    void delete(Long id);

    /** Aplica el orden recibido; debe incluir todos los ids existentes. */
    List<FeaturedLineResponse> reorder(List<Long> ids);
}
