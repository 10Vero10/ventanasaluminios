package com.ventanasyaluminios.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Mismos casos que las pruebas del configurador en el frontend (briefing §3.5.2, ADR-007). */
class GlassConfiguratorRulesTest {

    @Test
    void calculaElAreaConDosDecimales() {
        assertThat(GlassConfiguratorRules.areaM2(150, 120)).isEqualTo(new BigDecimal("1.80"));
        assertThat(GlassConfiguratorRules.areaM2(600, 260)).isEqualTo(new BigDecimal("15.60"));
        assertThat(GlassConfiguratorRules.areaM2(33, 33)).isEqualTo(new BigDecimal("0.11"));
        assertThat(GlassConfiguratorRules.areaM2(1, 1)).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    void proponeUnaHojaPorCada150CmDeAncho() {
        assertThat(GlassConfiguratorRules.suggestedPanes(1)).isEqualTo(1);
        assertThat(GlassConfiguratorRules.suggestedPanes(150)).isEqualTo(1);
        assertThat(GlassConfiguratorRules.suggestedPanes(151)).isEqualTo(2);
        assertThat(GlassConfiguratorRules.suggestedPanes(300)).isEqualTo(2);
        assertThat(GlassConfiguratorRules.suggestedPanes(600)).isEqualTo(4);
    }

    @Test
    void describeElVidrioEnMinusculas() {
        assertThat(GlassConfiguratorRules.describeGlass("Laminado templado", "Bronce", 8))
                .isEqualTo("Vidrio laminado templado bronce 8 mm");
        assertThat(GlassConfiguratorRules.describeGlass("Acústico", "Verde Frence", 6))
                .isEqualTo("Vidrio acústico verde frence 6 mm");
    }

    @Test
    void describeLasMedidasYAgregaLasHojasSoloSiHayMasDeUna() {
        String glass = "Vidrio laminado templado bronce 8 mm";

        assertThat(GlassConfiguratorRules.describeDimensions(150, 120, 1, glass))
                .isEqualTo("120 cm alto x 150 cm ancho (1.80 m²) · Vidrio laminado templado bronce 8 mm");
        assertThat(GlassConfiguratorRules.describeDimensions(450, 200, 3, glass))
                .isEqualTo("200 cm alto x 450 cm ancho (9.00 m²) · Vidrio laminado templado bronce 8 mm · 3 hojas");
    }
}
