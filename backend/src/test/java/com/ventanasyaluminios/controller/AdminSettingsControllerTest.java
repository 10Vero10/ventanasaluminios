package com.ventanasyaluminios.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ventanasyaluminios.service.GalleryService;
import com.ventanasyaluminios.service.SiteSettingsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Validaciones de entrada de la configuración y la galería, sin llegar al service. */
@WebMvcTest({AdminSettingsController.class, SiteController.class})
class AdminSettingsControllerTest {

    private static final String VALID = """
            {"heroTitle":"Ventanas y Aluminios SAS","heroSubtitle":"Subtítulo","heroImageUrl":"%s",
             "phone":"+57 317 846 3260","email":"%s","whatsappNumber":"","experienceYears":25,
             "quotingEnabled":true,"aboutParagraph1":"a","aboutParagraph2":"b","aboutHighlight":"c"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SiteSettingsService siteSettingsService;

    @MockitoBean
    private GalleryService galleryService;

    @Test
    void correoInvalidoDevuelveMensajeEnEspanol() throws Exception {
        mockMvc.perform(put("/api/v1/admin/settings").contentType(MediaType.APPLICATION_JSON)
                        .content(VALID.formatted("", "no-es-un-correo")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El correo no tiene un formato válido."))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        verifyNoInteractions(siteSettingsService);
    }

    @Test
    void imagenEnBase64OJavascriptSeRechaza() throws Exception {
        for (String url : new String[] {"data:image/png;base64,AAAA", "javascript:alert(1)"}) {
            mockMvc.perform(put("/api/v1/admin/settings").contentType(MediaType.APPLICATION_JSON)
                            .content(VALID.formatted(url, "ventanaluminio@hotmail.com")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("La imagen principal debe ser una URL http(s) válida."));
        }
        verifyNoInteractions(siteSettingsService);
    }

    @Test
    void lineaDeGaleriaInexistenteDevuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/gallery").param("category", "TECHOS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }
}
