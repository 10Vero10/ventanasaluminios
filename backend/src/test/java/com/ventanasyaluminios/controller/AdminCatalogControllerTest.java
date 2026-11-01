package com.ventanasyaluminios.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ventanasyaluminios.service.GlassOptionService;
import com.ventanasyaluminios.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Validaciones de entrada del catálogo en el panel: lo inválido nunca llega al service. */
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest({AdminProductController.class, AdminGlassOptionController.class})
class AdminCatalogControllerTest {

    private static final String PRODUCT = """
            {"category":"VENTANAS","title":"Proyectantes","subtitle":"Línea","description":"Desc","active":true,
             "specs":%s,"features":%s}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private GlassOptionService glassOptionService;

    @Test
    void filaDeFichaTecnicaSinEtiquetaSeRechaza() throws Exception {
        postInvalid("/api/v1/admin/products", PRODUCT.formatted("[{\"label\":\"\",\"value\":\"2\"}]", "[]"))
                .andExpect(jsonPath("$.error").value("Cada fila de la ficha técnica necesita una etiqueta."));
        verifyNoInteractions(productService);
    }

    @Test
    void caracteristicaVaciaSeRechaza() throws Exception {
        postInvalid("/api/v1/admin/products", PRODUCT.formatted("[]", "[\" \"]"))
                .andExpect(jsonPath("$.error").value("Hay una característica vacía."));
        verifyNoInteractions(productService);
    }

    @Test
    void colorConHexInvalidoOCodigoEnMinusculasSeRechaza() throws Exception {
        postInvalid("/api/v1/admin/glass-colors", """
                {"code":"AZUL","name":"Azul","hex":"azul","reflective":false,"active":true,"defaultOption":false}
                """).andExpect(jsonPath("$.error").value("El color debe estar en formato #RRGGBB (ej. #396DC6)."));
        postInvalid("/api/v1/admin/glass-colors", """
                {"code":"azul","name":"Azul","hex":"#0000FF","reflective":false,"active":true,"defaultOption":false}
                """).andExpect(jsonPath("$.error")
                .value("El código solo puede tener mayúsculas, números y guion bajo (ej. AZUL_LAGO)."));
        verifyNoInteractions(glassOptionService);
    }

    @Test
    void espesorFueraDeRangoSeRechaza() throws Exception {
        postInvalid("/api/v1/admin/glass-thicknesses", "{\"mm\":0,\"active\":true,\"defaultOption\":false}")
                .andExpect(jsonPath("$.error").value("El espesor debe estar entre 1 y 50 mm."));
        verifyNoInteractions(glassOptionService);
    }

    private ResultActions postInvalid(String url, String body) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
