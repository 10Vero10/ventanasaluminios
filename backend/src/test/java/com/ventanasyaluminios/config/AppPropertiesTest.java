package com.ventanasyaluminios.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;

class AppPropertiesTest {

    private static final AppProperties.RateLimit RATE_LIMIT = new AppProperties.RateLimit(
            new AppProperties.Rule(10, Duration.ofHours(1)), new AppProperties.Rule(5, Duration.ofMinutes(1)));
    private static final AppProperties.Auth AUTH = new AppProperties.Auth("x".repeat(32), Duration.ofHours(8), true,
            null, null);

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void corsConComodinSeRechazaAlArrancar() {
        var properties = new AppProperties(new AppProperties.Cors(List.of("*")),
                new AppProperties.Media("uploads", DataSize.ofMegabytes(5)), RATE_LIMIT, AUTH);

        assertThat(validator.validate(properties)).extracting(v -> v.getMessage())
                .containsExactly("app.cors.allowed-origins no puede contener '*': lista los orígenes exactos.");
    }

    @Test
    void elLimiteDeTamanoSeMuestraLegible() {
        assertThat(new AppProperties.Media("uploads", DataSize.ofMegabytes(5)).maxFileSizeLabel()).isEqualTo("5 MB");
        assertThat(new AppProperties.Media("uploads", DataSize.ofKilobytes(512)).maxFileSizeLabel()).isEqualTo("512 KB");
    }
}
