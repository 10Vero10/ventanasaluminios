package com.ventanasyaluminios.config;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ventanasyaluminios.controller.HealthController;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** El backend sirve el frontend compilado con {@code SITE_DIR} y solo la API sin ella (ADR-022). */
class WebConfigSiteTest {

    @Nested
    @AutoConfigureMockMvc(addFilters = false)
    @WebMvcTest(HealthController.class)
    class ConSitio {

        @TempDir
        static Path site;

        @Autowired
        private MockMvc mockMvc;

        /** El valor se lee al crear el contexto, después de {@link #createSite()}. */
        @DynamicPropertySource
        static void siteDir(DynamicPropertyRegistry registry) {
            registry.add("app.site.dir", () -> site.toString());
        }

        @BeforeAll
        static void createSite() throws IOException {
            Files.createDirectories(site.resolve("assets"));
            Files.writeString(site.resolve("index.html"), "<!doctype html><title>Ventanas</title>");
            Files.writeString(site.resolve("favicon.svg"), "<svg/>");
            Files.writeString(site.resolve("assets/app-abc123.js"), "console.log(1)");
        }

        @Test
        void laRaizReenviaAIndex() throws Exception {
            // MockMvc no ejecuta el reenvío; en el servidor real lo sirve el mismo manejador de las rutas de React.
            mockMvc.perform(get("/")).andExpect(forwardedUrl("/index.html"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"/index.html", "/catalogo", "/admin/vidrio"})
        void lasRutasDeReactDevuelvenIndexSinCache(String path) throws Exception {
            mockMvc.perform(get(path))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("<title>Ventanas</title>")))
                    .andExpect(header().string("Cache-Control", "no-cache"));
        }

        @Test
        void unArchivoQueExisteSeSirveTalCual() throws Exception {
            mockMvc.perform(get("/favicon.svg"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("<svg/>"));
        }

        @Test
        void losAssetsLlevanCacheLargaYLosQueNoExistenDan404() throws Exception {
            mockMvc.perform(get("/assets/app-abc123.js"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Cache-Control", "max-age=31536000, public, immutable"));
            mockMvc.perform(get("/assets/app-viejo.js")).andExpect(status().isNotFound());
        }

        @Test
        void laApiNoCaeEnElSitio() throws Exception {
            mockMvc.perform(get("/api/v1/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ok"));
            mockMvc.perform(get("/api/v1/no-existe"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("NOT_FOUND"));
        }
    }

    @Nested
    @AutoConfigureMockMvc(addFilters = false)
    @WebMvcTest(HealthController.class)
    class SinSitio {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void soloAtiendeLaApi() throws Exception {
            mockMvc.perform(get("/api/v1/health")).andExpect(status().isOk());
            mockMvc.perform(get("/catalogo")).andExpect(status().isNotFound());
            mockMvc.perform(get("/")).andExpect(status().isNotFound());
        }
    }
}
