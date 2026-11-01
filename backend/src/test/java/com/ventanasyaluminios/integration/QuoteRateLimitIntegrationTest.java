package com.ventanasyaluminios.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ventanasyaluminios.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Límite de envíos por IP a través de la cadena real de filtros, con el límite reducido a 2 por hora. */
@SpringBootTest(properties = "app.rate-limit.quotes.max-requests=2")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class QuoteRateLimitIntegrationTest {

    /** El campo trampa evita escribir en la base: el límite cuenta igual. */
    private static final String BOT = "{\"name\":\"x\",\"phone\":\"1\",\"website\":\"x\"}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void alPasarElLimiteResponde429ConRetryAfterYOtraIpSigue() throws Exception {
        submitFrom("10.9.0.1").andExpect(status().isCreated());
        submitFrom("10.9.0.1").andExpect(status().isCreated());

        submitFrom("10.9.0.1")
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.error").value("Has enviado muchas solicitudes. Intenta más tarde."))
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"));

        submitFrom("10.9.0.2").andExpect(status().isCreated());
    }

    private ResultActions submitFrom(String ip) throws Exception {
        return mockMvc.perform(post("/api/v1/quotes").contentType(MediaType.APPLICATION_JSON).content(BOT)
                .with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                }));
    }
}
