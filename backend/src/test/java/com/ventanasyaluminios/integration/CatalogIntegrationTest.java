package com.ventanasyaluminios.integration;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.ventanasyaluminios.AdminTestAuthentication;
import com.ventanasyaluminios.TestcontainersConfiguration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Módulo catalog de punta a punta contra PostgreSQL real. Cada prueba se deshace al terminar; lo que solo se ve al
 * confirmar (restricciones diferibles, ediciones cruzadas) está en {@link CatalogCommitIntegrationTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import({TestcontainersConfiguration.class, AdminTestAuthentication.class})
class CatalogIntegrationTest {

    private static final String HOJAS = "{\"label\":\"Hojas\",\"value\":\"1\"}";
    private static final String CIERRE = "{\"label\":\"Cierre\",\"value\":\"Multipunto\"}";

    /** Producto nuevo; {@code %s} son si está activo y su ficha técnica. */
    private static final String NEW_PRODUCT = """
            {"category":"VENTANAS","categoryNumber":"01","title":"Proyectantes","subtitle":"Apertura hacia afuera",
             "description":"Ventilación con lluvia.","imageUrl":"","badge":"","ctaText":"Cotizar proyectante",
             "active":%s,"specs":[%s],"features":["Brazos de fricción","Empaque EPDM"]}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void elCatalogoPublicoTraeFichaYCaracteristicasYFiltraPorLinea() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].specs[0].label").exists())
                .andExpect(jsonPath("$[0].features[0]").exists());

        mockMvc.perform(get("/api/v1/products").param("category", "PORTONES"))
                .andExpect(jsonPath("$[*].category", everyItem(is("PORTONES"))));
    }

    @Test
    void crearEditarYReordenarLaFichaDeUnProducto() throws Exception {
        List<Integer> existing = JsonPath.read(json(get("/api/v1/admin/products")), "$[*].id");
        int id = JsonPath.read(send(post("/api/v1/admin/products"), NEW_PRODUCT.formatted(true, HOJAS + "," + CIERRE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.position").value(existing.size() + 1))
                .andExpect(jsonPath("$.imageUrl").doesNotExist())
                .andReturn().getResponse().getContentAsString(), "$.id");

        send(put("/api/v1/admin/products/" + id), NEW_PRODUCT.formatted(true, CIERRE + "," + HOJAS))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/products/" + id))
                .andExpect(jsonPath("$.specs[0].label").value("Cierre"))
                .andExpect(jsonPath("$.specs[1].label").value("Hojas"));
    }

    @Test
    void unProductoOcultoNoApareceEnElSitioPeroSiEnElPanel() throws Exception {
        int id = JsonPath.read(send(post("/api/v1/admin/products"), NEW_PRODUCT.formatted(false, HOJAS))
                .andReturn().getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/v1/products/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("El producto no existe."));
        mockMvc.perform(get("/api/v1/products")).andExpect(jsonPath("$[*].id", not(hasItem(id))));
        mockMvc.perform(get("/api/v1/admin/products")).andExpect(jsonPath("$[*].id", hasItem(id)));
    }

    @Test
    void duplicarCreaUnaCopiaOcultaYEliminarLaQuita() throws Exception {
        String products = json(get("/api/v1/admin/products"));
        int sourceId = JsonPath.read(products, "$[0].id");
        String sourceTitle = JsonPath.read(products, "$[0].title");
        List<Object> sourceSpecs = JsonPath.read(products, "$[0].specs");

        int copyId = JsonPath.read(mockMvc.perform(post("/api/v1/admin/products/" + sourceId + "/duplicate"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(sourceTitle + " (copia)"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.specs", hasSize(sourceSpecs.size())))
                .andReturn().getResponse().getContentAsString(), "$.id");

        mockMvc.perform(delete("/api/v1/admin/products/" + copyId)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/admin/products/" + copyId)).andExpect(status().isNotFound());
    }

    @Test
    void reordenarElCatalogo() throws Exception {
        List<Integer> ids = new ArrayList<>(JsonPath.read(json(get("/api/v1/admin/products")), "$[*].id"));
        Integer last = ids.removeLast();
        ids.addFirst(last);

        send(put("/api/v1/admin/products/order"), "{\"ids\":" + ids + "}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(last))
                .andExpect(jsonPath("$[0].position").value(1));
    }

    @Test
    void lasOpcionesDeVidrioPublicasSonLasActivasConSuValorPorDefecto() throws Exception {
        mockMvc.perform(get("/api/v1/glass-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.types[*].active", everyItem(is(true))))
                .andExpect(jsonPath("$.types[?(@.defaultOption == true)].code").value("CRUDO"))
                .andExpect(jsonPath("$.colors[?(@.code == 'VERDE_FRENCE')].name").value("Verde Frence"))
                .andExpect(jsonPath("$.colors[?(@.reflective == true)].note")
                        .value("Mismo tono que Azul intenso, con acabado reflectivo hacia el exterior."))
                .andExpect(jsonPath("$.thicknesses[?(@.defaultOption == true)].mm").value(6));
    }

    @Test
    void cambiarLaOpcionPorDefectoDesmarcaLaAnteriorEnLaBase() throws Exception {
        int templadoId = idOf("$.types[?(@.code == 'TEMPLADO')].id");

        send(put("/api/v1/admin/glass-types/" + templadoId),
                "{\"code\":\"TEMPLADO\",\"name\":\"Templado\",\"active\":true,\"defaultOption\":true}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.defaultOption").value(true));

        mockMvc.perform(get("/api/v1/glass-options"))
                .andExpect(jsonPath("$.types[?(@.defaultOption == true)]", hasSize(1)))
                .andExpect(jsonPath("$.types[?(@.defaultOption == true)].code").value("TEMPLADO"));
    }

    @Test
    void desactivarUnaOpcionLaQuitaDelConfiguradorPeroNoDelPanel() throws Exception {
        int bronceId = idOf("$.colors[?(@.code == 'BRONCE')].id");

        send(put("/api/v1/admin/glass-colors/" + bronceId), """
                {"code":"BRONCE","name":"Bronce","hex":"#966969","reflective":false,"active":false,"defaultOption":false}
                """).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/glass-options"))
                .andExpect(jsonPath("$.colors[*].code", not(hasItem("BRONCE"))))
                .andExpect(jsonPath("$.colors[*].active", everyItem(is(true))));
        mockMvc.perform(get("/api/v1/admin/glass-options"))
                .andExpect(jsonPath("$.colors[?(@.code == 'BRONCE')].active").value(false));
    }

    @Test
    void codigoRepetidoOCambioDeCodigoSeRechazan() throws Exception {
        send(post("/api/v1/admin/glass-thicknesses"), "{\"mm\":6,\"active\":true,\"defaultOption\":false}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_CODE"));

        int crudoId = idOf("$.types[?(@.code == 'CRUDO')].id");
        send(put("/api/v1/admin/glass-types/" + crudoId),
                "{\"code\":\"BASICO\",\"name\":\"Crudo\",\"active\":true,\"defaultOption\":true}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("IMMUTABLE_FIELD"));
    }

    private int idOf(String path) throws Exception {
        List<Integer> ids = JsonPath.read(json(get("/api/v1/admin/glass-options")), path);
        return ids.getFirst();
    }

    private String json(RequestBuilder request) throws Exception {
        return mockMvc.perform(request).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    private ResultActions send(MockHttpServletRequestBuilder request, String body) throws Exception {
        return mockMvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
