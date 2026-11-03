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
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** El backend sirve el frontend compilado con las mismas reglas que nginx (ADR-022). */
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(HealthController.class)
class SiteConfigTest {

    private static final Path SITE = createSite();

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void siteDir(DynamicPropertyRegistry registry) {
        registry.add("app.site.dir", SITE::toString);
    }

    private static Path createSite() {
        try {
            Path dir = Files.createTempDirectory("sitio");
            Files.createDirectories(dir.resolve("assets"));
            Files.writeString(dir.resolve("index.html"), "<!doctype html><title>Ventanas</title>");
            Files.writeString(dir.resolve("favicon.svg"), "<svg/>");
            Files.writeString(dir.resolve("assets/app-abc123.js"), "console.log(1)");
            return dir;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void laRaizReenviaAIndex() throws Exception {
        // MockMvc no ejecuta el reenvío; en el servidor real lo sirve el mismo manejador de las rutas de React.
        mockMvc.perform(get("/")).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void lasRutasDeReactDevuelvenIndexSinCache() throws Exception {
        for (String path : new String[] {"/index.html", "/catalogo", "/admin/vidrio"}) {
            mockMvc.perform(get(path))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("<title>Ventanas</title>")))
                    .andExpect(header().string("Cache-Control", "no-cache"));
        }
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
        mockMvc.perform(get("/media/no-existe.png")).andExpect(status().isNotFound());
    }
}
