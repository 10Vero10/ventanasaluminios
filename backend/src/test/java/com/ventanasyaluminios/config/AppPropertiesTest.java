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

    @Test
    void lasClavesDeEjemploDeEnvExampleSeRechazanAlArrancar() {
        var auth = new AppProperties.Auth("cambiar-por-una-clave-larga-y-aleatoria-de-al-menos-32-caracteres",
                Duration.ofHours(8), true, "admin@ventanasyaluminios.com", "cambiar-esta-clave-temporal");

        assertThat(validator.validate(auth)).extracting(v -> v.getMessage()).containsExactlyInAnyOrder(
                "JWT_SECRET sigue con el valor de ejemplo de .env.example: genera una clave propia.",
                "ADMIN_INITIAL_PASSWORD sigue con el valor de ejemplo de .env.example: elige una contraseña propia.");
    }

    @Test
    void clavesPropiasPasanLaValidacion() {
        var auth = new AppProperties.Auth("una-clave-propia-y-larga-de-mas-de-32-caracteres", Duration.ofHours(8), true,
                "admin@empresa.com", "Temporal-propia-2026");

        assertThat(validator.validate(auth)).isEmpty();
    }
}
