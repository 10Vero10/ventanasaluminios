package com.ventanasyaluminios.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.ventanasyaluminios.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * Con Tomcat real: un encabezado "Forwarded" que manda el cliente no cambia la IP con la que se cuenta el límite.
 * Antes, con forward-headers-strategy=framework, rotar ese encabezado anulaba el límite.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.rate-limit.quotes.max-requests=2")
@Import(TestcontainersConfiguration.class)
class ForwardedHeaderIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void rotarElEncabezadoForwardedNoEvadeElLimite() {
        assertThat(submitClaiming("198.51.100.1")).isEqualTo(HttpStatus.CREATED);
        assertThat(submitClaiming("198.51.100.2")).isEqualTo(HttpStatus.CREATED);

        assertThat(submitClaiming("198.51.100.3")).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    private HttpStatus submitClaiming(String fakeIp) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Forwarded", "for=" + fakeIp);
        // Campo trampa: no escribe en la base, pero el límite cuenta igual.
        String bot = "{\"name\":\"x\",\"phone\":\"1\",\"website\":\"x\"}";
        return HttpStatus.valueOf(rest.postForEntity("/api/v1/quotes", new HttpEntity<>(bot, headers), String.class)
                .getStatusCode().value());
    }
}
