package com.ventanasyaluminios;

import static org.assertj.core.api.Assertions.assertThat;

import com.ventanasyaluminios.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

/** Arranque completo contra PostgreSQL real: migraciones, validación del esquema y configuración. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class ApplicationIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private AppProperties properties;

    @Test
    void arrancaConPostgresYRespondeHealth() {
        var response = rest.getForEntity("/api/v1/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"ok\"");
    }

    @Test
    void sinOrigenesConfiguradosCorsQuedaDesactivado() {
        assertThat(properties.cors().allowedOrigins()).isEmpty();
        assertThat(properties.media().maxFileSize().toMegabytes()).isEqualTo(5);
    }
}
