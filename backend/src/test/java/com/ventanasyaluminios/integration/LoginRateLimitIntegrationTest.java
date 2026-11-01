package com.ventanasyaluminios.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

/** Límite de intentos de login por IP, reducido a 2 por minuto para la prueba. */
@SpringBootTest(properties = "app.rate-limit.login.max-requests=2")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class LoginRateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void alPasarElLimiteDeIntentosResponde429ConElMensajeDelBriefing() throws Exception {
        attempt().andExpect(status().isUnauthorized());
        attempt().andExpect(status().isUnauthorized());

        attempt()
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("Demasiados intentos. Espera un minuto."))
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"));
    }

    private ResultActions attempt() throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nadie@empresa.com\",\"password\":\"clave-equivocada\"}")
                .with(request -> {
                    request.setRemoteAddr("10.30.0.1");
                    return request;
                }));
    }
}
