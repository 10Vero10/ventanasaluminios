package com.ventanasyaluminios;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/** Arranque completo contra PostgreSQL real (migraciones y validación del esquema) con Tomcat real. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class ApplicationIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void arrancaConPostgresYRespondeHealth() {
        var response = rest.getForEntity("/api/v1/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"ok\"");
    }

    @Test
    void sinOrigenesConfiguradosOtroDominioNoRecibePermisoCors() {
        HttpHeaders headers = new HttpHeaders();
        headers.setOrigin("https://sitio-ajeno.com");
        headers.setAccessControlRequestMethod(HttpMethod.PUT);

        var response = rest.exchange("/api/v1/admin/settings", HttpMethod.OPTIONS, new HttpEntity<>(headers),
                String.class);

        assertThat(response.getHeaders().getAccessControlAllowOrigin()).isNull();
    }
}
