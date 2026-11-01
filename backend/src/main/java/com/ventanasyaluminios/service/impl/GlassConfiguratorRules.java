package com.ventanasyaluminios.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Reglas del configurador de vidrio (briefing §3.5 y ADR-007). El frontend las replica para dibujar el plano; el
 * servidor las recalcula al recibir una cotización y no confía en lo que mande el navegador. Ambos lados se prueban
 * con los mismos casos.
 */
final class GlassConfiguratorRules {

    static final int MAX_WIDTH_CM = 600;
    static final int MAX_HEIGHT_CM = 260;
    static final int MAX_PANE_WIDTH_CM = 150;
    static final int MAX_PANES = 4;

    private static final Locale SPANISH = Locale.forLanguageTag("es-CO");

    private GlassConfiguratorRules() {
    }

    /** Área en m² con dos decimales: ancho × alto / 10 000. */
    static BigDecimal areaM2(int widthCm, int heightCm) {
        return BigDecimal.valueOf((long) widthCm * heightCm).divide(BigDecimal.valueOf(10_000), 2, RoundingMode.HALF_UP);
    }

    /** Hojas que propone el configurador: una por cada 150 cm de ancho, entre 1 y 4. */
    static int suggestedPanes(int widthCm) {
        int panes = (widthCm + MAX_PANE_WIDTH_CM - 1) / MAX_PANE_WIDTH_CM;
        return Math.min(MAX_PANES, Math.max(1, panes));
    }

    /** "Vidrio laminado templado bronce 8 mm". */
    static String describeGlass(String typeName, String colorName, int thicknessMm) {
        return "Vidrio " + typeName.toLowerCase(SPANISH) + " " + colorName.toLowerCase(SPANISH) + " " + thicknessMm + " mm";
    }

    /**
     * Texto de medidas que se guarda con la cotización:
     * "120 cm alto x 150 cm ancho (1.80 m²) · Vidrio crudo transparente 6 mm", más " · 3 hojas" si hay más de una.
     */
    static String describeDimensions(int widthCm, int heightCm, int panes, String glassDescription) {
        String text = heightCm + " cm alto x " + widthCm + " cm ancho (" + areaM2(widthCm, heightCm).toPlainString()
                + " m²) · " + glassDescription;
        return panes > 1 ? text + " · " + panes + " hojas" : text;
    }
}
