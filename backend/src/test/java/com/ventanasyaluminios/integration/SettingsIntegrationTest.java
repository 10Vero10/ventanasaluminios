package com.ventanasyaluminios.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.ventanasyaluminios.TestImages;
import com.ventanasyaluminios.AdminTestAuthentication;
import com.ventanasyaluminios.TestcontainersConfiguration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Módulo settings de punta a punta contra PostgreSQL real. Cada prueba se deshace al terminar. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import({TestcontainersConfiguration.class, AdminTestAuthentication.class})
class SettingsIntegrationTest {

    private static final Path UPLOAD_DIR = createTempDir();

    @DynamicPropertySource
    static void mediaDir(DynamicPropertyRegistry registry) {
        registry.add("app.media.upload-dir", UPLOAD_DIR::toString);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void elSitioPublicoTraeLaConfiguracionYLasLineasEnOrden() throws Exception {
        mockMvc.perform(get("/api/v1/site"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heroTitle").value("Ventanas y Aluminios SAS"))
                .andExpect(jsonPath("$.experienceYears").value(25))
                .andExpect(jsonPath("$.quotingEnabled").value(true))
                .andExpect(jsonPath("$.featuredLines", hasSize(4)))
                .andExpect(jsonPath("$.featuredLines[0].title").value("Ventanas"))
                .andExpect(jsonPath("$.featuredLines[3].targetCategory").value("DIVISIONES"));
    }

    @Test
    void editarLaConfiguracionSeVeAlInstanteEnElSitio() throws Exception {
        String body = """
                {"heroTitle":"Nuevo título","heroSubtitle":"Subtítulo","heroImageUrl":"",
                 "heroBadge":"","phone":"+57 300 111 2233","email":"ventas@empresa.com","whatsappNumber":"",
                 "experienceYears":26,"quotingEnabled":false,"faviconUrl":"/favicon.svg",
                 "aboutParagraph1":"a","aboutParagraph2":"b","aboutHighlight":"c"}
                """;
        mockMvc.perform(put("/api/v1/admin/settings").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.whatsappNumber").value("573001112233"));

        mockMvc.perform(get("/api/v1/site"))
                .andExpect(jsonPath("$.heroTitle").value("Nuevo título"))
                .andExpect(jsonPath("$.experienceYears").value(26))
                .andExpect(jsonPath("$.quotingEnabled").value(false))
                .andExpect(jsonPath("$.heroImageUrl").doesNotExist());
    }

    @Test
    void galeriaCrearFiltrarReordenarYEliminar() throws Exception {
        List<Integer> existingIds = JsonPath.read(getJson("/api/v1/admin/gallery"), "$[*].id");

        String created = mockMvc.perform(post("/api/v1/admin/gallery").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"url\":\"https://ejemplo.com/porton.jpg\",\"category\":\"PORTONES\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Imagen"))
                .andExpect(jsonPath("$.position").value(existingIds.size() + 1))
                .andReturn().getResponse().getContentAsString();
        int newId = JsonPath.read(created, "$.id");

        mockMvc.perform(get("/api/v1/gallery").param("category", "PORTONES"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(newId));

        List<Integer> newOrder = new ArrayList<>(existingIds);
        newOrder.addFirst(newId);
        mockMvc.perform(put("/api/v1/admin/gallery/order").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":" + newOrder + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(newId))
                .andExpect(jsonPath("$[0].position").value(1));

        mockMvc.perform(delete("/api/v1/admin/gallery/" + newId)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/admin/gallery/" + newId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("La imagen no existe."));
    }

    @Test
    void whatsappDerivadoInvalidoDevuelve400YNoGuardaNada() throws Exception {
        String body = """
                {"heroTitle":"Otro título","heroSubtitle":"Subtítulo","phone":"+57 300 111 2233 444 555 666 777 88",
                 "email":"ventas@empresa.com","whatsappNumber":"","experienceYears":25,"quotingEnabled":true,
                 "aboutParagraph1":"a","aboutParagraph2":"b","aboutHighlight":"c"}
                """;
        mockMvc.perform(put("/api/v1/admin/settings").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        mockMvc.perform(get("/api/v1/site")).andExpect(jsonPath("$.heroTitle").value("Ventanas y Aluminios SAS"));
    }

    @Test
    void reordenarConIdsQueNoCoincidenSeRechaza() throws Exception {
        List<Integer> ids = JsonPath.read(getJson("/api/v1/admin/featured-lines"), "$[*].id");
        mockMvc.perform(put("/api/v1/admin/featured-lines/order").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":" + ids.subList(0, ids.size() - 1) + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REORDER_MISMATCH"));
    }

    @Test
    void subirUnaImagenYDescargarlaPorMedia() throws Exception {
        String uploaded = mockMvc.perform(multipart("/api/v1/admin/media")
                        .file(new MockMultipartFile("file", "foto.png", "image/png", TestImages.PNG)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url", startsWith("/media/")))
                .andReturn().getResponse().getContentAsString();
        String url = JsonPath.read(uploaded, "$.url");

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().bytes(TestImages.PNG));
    }

    @Test
    void subirAlgoQueNoEsImagenSeRechaza() throws Exception {
        mockMvc.perform(multipart("/api/v1/admin/media")
                        .file(new MockMultipartFile("file", "virus.png", "image/png", "MZ".getBytes())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FILE_TYPE"));
    }

    private String getJson(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    private static Path createTempDir() {
        try {
            return Files.createTempDirectory("vya-media-test");
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
