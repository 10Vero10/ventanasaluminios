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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** Módulo catalog de punta a punta contra PostgreSQL real. Cada prueba se deshace al terminar. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestcontainersConfiguration.class)
class CatalogIntegrationTest {

    private static final String NEW_PRODUCT = """
            {"category":"VENTANAS","categoryNumber":"01","title":"Proyectantes","subtitle":"Apertura hacia afuera",
             "description":"Ventilación con lluvia.","imageUrl":"","badge":"","ctaText":"Cotizar proyectante",
             "active":%s,
             "specs":[{"label":"Hojas","value":"1"},{"label":"Cierre","value":"Multipunto"}],
             "features":["Brazos de fricción","Empaque EPDM"]}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void elCatalogoPublicoTraeLosOchoProductosYFiltraPorLinea() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[0].title").value("Corredizas"))
                .andExpect(jsonPath("$[0].specs[0].label").value("Hojas"))
                .andExpect(jsonPath("$[0].features", hasSize(3)));

        mockMvc.perform(get("/api/v1/products").param("category", "PORTONES"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ctaText").value("Cotizar portón americano"));
    }

    @Test
    void crearEditarYReordenarLaFichaDeUnProducto() throws Exception {
        List<Integer> existing = JsonPath.read(json(get("/api/v1/admin/products")), "$[*].id");
        int id = JsonPath.read(send(post("/api/v1/admin/products"), NEW_PRODUCT.formatted(true))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.position").value(existing.size() + 1))
                .andExpect(jsonPath("$.imageUrl").doesNotExist())
                .andReturn().getResponse().getContentAsString(), "$.id");

        String reordered = NEW_PRODUCT.formatted(true)
                .replace("[{\"label\":\"Hojas\",\"value\":\"1\"},{\"label\":\"Cierre\",\"value\":\"Multipunto\"}]",
                        "[{\"label\":\"Cierre\",\"value\":\"Multipunto\"},{\"label\":\"Hojas\",\"value\":\"1\"}]");
        send(put("/api/v1/admin/products/" + id), reordered).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/products/" + id))
                .andExpect(jsonPath("$.specs[0].label").value("Cierre"))
                .andExpect(jsonPath("$.specs[1].label").value("Hojas"));
    }

    @Test
    void unProductoOcultoNoApareceEnElSitioPeroSiEnElPanel() throws Exception {
        int id = JsonPath.read(send(post("/api/v1/admin/products"), NEW_PRODUCT.formatted(false))
                .andReturn().getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/v1/products/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("El producto no existe."));
        mockMvc.perform(get("/api/v1/products")).andExpect(jsonPath("$[*].id", not(hasItem(id))));
        mockMvc.perform(get("/api/v1/admin/products")).andExpect(jsonPath("$[*].id", hasItem(id)));
    }

    @Test
    void duplicarCreaUnaCopiaOcultaYEliminarLaQuita() throws Exception {
        List<Integer> ids = JsonPath.read(json(get("/api/v1/admin/products")), "$[*].id");
        int copyId = JsonPath.read(mockMvc.perform(post("/api/v1/admin/products/" + ids.get(0) + "/duplicate"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Corredizas (copia)"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.specs", hasSize(3)))
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
                .andExpect(jsonPath("$.types", hasSize(6)))
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
        mockMvc.perform(get("/api/v1/admin/glass-options")).andExpect(jsonPath("$.colors", hasSize(7)));
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

    private String json(org.springframework.test.web.servlet.RequestBuilder request) throws Exception {
        return mockMvc.perform(request).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    private ResultActions send(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
                               String body) throws Exception {
        return mockMvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
