package com.ventanasyaluminios.mapper;

import static com.ventanasyaluminios.mapper.TextNormalization.trimToNull;

import com.ventanasyaluminios.dto.request.QuoteCreateRequest;
import com.ventanasyaluminios.dto.response.QuoteDetailResponse;
import com.ventanasyaluminios.dto.response.QuoteResponse;
import com.ventanasyaluminios.dto.response.QuoteStatusChangeResponse;
import com.ventanasyaluminios.entity.Quote;
import com.ventanasyaluminios.entity.QuoteStatusChange;
import java.util.List;
import org.springframework.stereotype.Component;

/** Conversión entre entidades y DTO del módulo quote. */
@Component
public class QuoteMapper {

    public QuoteResponse toResponse(Quote quote) {
        return new QuoteResponse(
                quote.getId(),
                quote.getCreatedAt(),
                quote.getUpdatedAt(),
                quote.getStatus(),
                quote.getName(),
                quote.getPhone(),
                quote.getProductType(),
                quote.getDimensionsText(),
                quote.getMessage(),
                quote.getWidthCm(),
                quote.getHeightCm(),
                quote.getPanes(),
                quote.getAreaM2(),
                quote.getGlassTypeCode(),
                quote.getGlassColorCode(),
                quote.getThicknessMm());
    }

    public QuoteDetailResponse toDetailResponse(Quote quote, List<QuoteStatusChange> history) {
        return new QuoteDetailResponse(toResponse(quote), history.stream()
                .map(change -> new QuoteStatusChangeResponse(change.getFromStatus(), change.getToStatus(),
                        change.getChangedAt()))
                .toList());
    }

    /** Copia los textos del formulario recortando espacios; los opcionales vacíos quedan nulos. */
    public void apply(QuoteCreateRequest request, Quote quote) {
        quote.setName(request.name().trim());
        quote.setPhone(request.phone().trim());
        quote.setProductType(trimToNull(request.productType()));
        quote.setDimensionsText(trimToNull(request.dimensionsText()));
        quote.setMessage(trimToNull(request.message()));
    }
}
