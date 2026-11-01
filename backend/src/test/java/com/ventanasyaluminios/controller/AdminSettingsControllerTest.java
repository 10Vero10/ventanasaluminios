package com.ventanasyaluminios.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ventanasyaluminios.service.FeaturedLineService;
import com.ventanasyaluminios.service.SiteSettingsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Validaciones de entrada del panel: la solicitud inválida nunca llega al service. */
@WebMvcTest({AdminSettingsController.class, AdminFeaturedLineController.class})
class AdminSettingsControllerTest {

    private static final String SETTINGS = """
            {"heroTitle":"Ventanas y Aluminios SAS","heroSubtitle":"Subtítulo","heroImageUrl":"%s",
             "phone":"%s","email":"%s","whatsappNumber":"","experienceYears":25,
             "quotingEnabled":true,"aboutParagraph1":"a","aboutParagraph2":"b","aboutHighlight":"c"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SiteSettingsService siteSettingsService;

    @MockitoBean
    private FeaturedLineService featuredLineService;

    @Test
    void correoInvalidoDevuelveMensajeEnEspanol() throws Exception {
        putSettings("", "+57 317 846 3260", "no-es-un-correo")
                .andExpect(jsonPath("$.error").value("El correo no tiene un formato válido."))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        verifyNoInteractions(siteSettingsService);
    }

    @Test
    void telefonoSinDigitosSeRechaza() throws Exception {
        putSettings("", "(((--)))", "ventanaluminio@hotmail.com")
                .andExpect(jsonPath("$.error")
                        .value("El teléfono debe tener números y solo puede incluir espacios, +, guiones y paréntesis."));
        verifyNoInteractions(siteSettingsService);
    }

    @Test
    void imagenEnBase64JavascriptOHaciaOtroDominioSeRechaza() throws Exception {
        for (String url : new String[] {"data:image/png;base64,AAAA", "javascript:alert(1)", "//otro.com/x.png",
                "/\\\\otro.com/x.png"}) {
            putSettings(url, "+57 317 846 3260", "ventanaluminio@hotmail.com")
                    .andExpect(jsonPath("$.error").value("La imagen principal debe ser una URL http(s) válida."));
        }
        verifyNoInteractions(siteSettingsService);
    }

    @Test
    void reordenarConListaVaciaSeRechaza() throws Exception {
        mockMvc.perform(put("/api/v1/admin/featured-lines/order").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La lista de orden no puede estar vacía."));
        verifyNoInteractions(featuredLineService);
    }

    private org.springframework.test.web.servlet.ResultActions putSettings(String imageUrl, String phone, String email)
            throws Exception {
        return mockMvc.perform(put("/api/v1/admin/settings").contentType(MediaType.APPLICATION_JSON)
                        .content(SETTINGS.formatted(imageUrl, phone, email)))
                .andExpect(status().isBadRequest());
    }
}
