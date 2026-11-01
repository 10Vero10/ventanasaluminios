package com.ventanasyaluminios.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ventanasyaluminios.entity.enums.ProductCategory;
import com.ventanasyaluminios.service.GalleryService;
import com.ventanasyaluminios.service.SiteSettingsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Endpoints públicos del sitio: parámetros y elección del listado de la galería. */
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(SiteController.class)
class SiteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SiteSettingsService siteSettingsService;

    @MockitoBean
    private GalleryService galleryService;

    @Test
    void galeriaSinLineaDevuelveTodo() throws Exception {
        mockMvc.perform(get("/api/v1/gallery")).andExpect(status().isOk());
        verify(galleryService).list();
    }

    @Test
    void galeriaConLineaFiltra() throws Exception {
        mockMvc.perform(get("/api/v1/gallery").param("category", "PUERTAS")).andExpect(status().isOk());
        verify(galleryService).listByCategory(ProductCategory.PUERTAS);
    }

    @Test
    void lineaInexistenteDevuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/gallery").param("category", "TECHOS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }
}
