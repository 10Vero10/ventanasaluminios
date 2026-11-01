package com.ventanasyaluminios.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ventanasyaluminios.AdminTestAuthentication;
import com.ventanasyaluminios.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** Respaldo de punta a punta contra PostgreSQL real. Cada prueba se deshace al terminar. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import({TestcontainersConfiguration.class, AdminTestAuthentication.class})
class BackupIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void exportarEImportarElMismoRespaldoDejaTodoIgual() throws Exception {
        String exported = mockMvc.perform(get("/api/v1/admin/backup"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.content.settings.heroTitle").exists())
                .andReturn().getResponse().getContentAsString();
        ObjectNode backup = (ObjectNode) objectMapper.readTree(exported);
        int products = backup.path("products").size();

        importing(exported)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").value(products));

        String again = mockMvc.perform(get("/api/v1/admin/backup")).andReturn().getResponse().getContentAsString();
        ObjectNode reExported = (ObjectNode) objectMapper.readTree(again);
        reExported.remove("exportedAt");
        backup.remove("exportedAt");
        org.assertj.core.api.Assertions.assertThat(reExported).isEqualTo(backup);
    }

    @Test
    void unaOpcionDeVidrioAusenteEnElRespaldoSeDesactivaYNoSeBorra() throws Exception {
        ObjectNode backup = (ObjectNode) objectMapper.readTree(
                mockMvc.perform(get("/api/v1/admin/backup")).andReturn().getResponse().getContentAsString());
        ArrayNode colors = (ArrayNode) backup.path("content").path("glassOptions").path("colors");
        for (int i = 0; i < colors.size(); i++) {
            if ("BRONCE".equals(colors.get(i).path("code").asText())) {
                colors.remove(i);
                break;
            }
        }

        importing(backup.toString()).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/glass-options"))
                .andExpect(jsonPath("$.colors[?(@.code == 'BRONCE')].active").value(false));
    }

    @Test
    void archivosInvalidosRespondenConLosMensajesDelBriefing() throws Exception {
        importing("esto no es json")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El archivo no es un JSON válido."))
                .andExpect(jsonPath("$.code").value("INVALID_BACKUP"));
        importing("{\"version\":1,\"products\":[]}")
                .andExpect(jsonPath("$.error").value("El archivo no parece un respaldo del sitio (falta \"content\")."));
        importing("{\"version\":1,\"content\":{},\"products\":{}}")
                .andExpect(jsonPath("$.error")
                        .value("El respaldo tiene un catálogo inválido (\"products\" debe ser una lista)."));
    }

    @Test
    void unRespaldoConDatosInvalidosNoCambiaNada() throws Exception {
        ObjectNode backup = (ObjectNode) objectMapper.readTree(
                mockMvc.perform(get("/api/v1/admin/backup")).andReturn().getResponse().getContentAsString());
        ((ObjectNode) backup.path("content").path("settings")).put("email", "no-es-un-correo");

        importing(backup.toString())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El respaldo no es válido: El correo no tiene un formato válido."));
    }

    private ResultActions importing(String body) throws Exception {
        return mockMvc.perform(post("/api/v1/admin/backup").contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
