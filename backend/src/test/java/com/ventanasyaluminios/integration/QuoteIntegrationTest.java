package com.ventanasyaluminios.integration;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.ventanasyaluminios.AdminTestAuthentication;
import com.ventanasyaluminios.TestcontainersConfiguration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * Módulo quote de punta a punta contra PostgreSQL real. Cada prueba se deshace al terminar. Cada envío usa una IP
 * distinta para no tocar el límite por IP, que se prueba en {@link QuoteRateLimitIntegrationTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import({TestcontainersConfiguration.class, AdminTestAuthentication.class})
class QuoteIntegrationTest {

    private static final AtomicInteger NEXT_IP = new AtomicInteger(1);

    private static final String WITH_CONFIGURATOR = """
            {"name":"Carlos Mendoza","phone":"+57 300 000 0000","productType":"Ventanas arquitectónicas",
             "dimensionsText":"lo que mande el navegador","message":"Es para un segundo piso.",
             "widthCm":150,"heightCm":120,"glassTypeCode":"LAMINADO_TEMPLADO","glassColorCode":"BRONCE","thicknessMm":8}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unaCotizacionConConfiguradorGuardaLoCalculadoPorElServidor() throws Exception {
        String id = JsonPath.read(submit(WITH_CONFIGURATOR).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/v1/admin/quotes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quote.status").value("NUEVA"))
                .andExpect(jsonPath("$.quote.areaM2").value(1.80))
                .andExpect(jsonPath("$.quote.panes").value(1))
                .andExpect(jsonPath("$.quote.dimensionsText")
                        .value("120 cm alto x 150 cm ancho (1.80 m²) · Vidrio laminado templado bronce 8 mm"))
                .andExpect(jsonPath("$.history", hasSize(0)));
    }

    @Test
    void elCampoTrampaNoGuardaNada() throws Exception {
        long before = total();

        submit("{\"name\":\"Bot\",\"phone\":\"1\",\"website\":\"http://spam\"}").andExpect(status().isCreated());

        org.assertj.core.api.Assertions.assertThat(total()).isEqualTo(before);
    }

    @Test
    void sinNombreYTelefonoResponde400ConElMensajeDelBriefing() throws Exception {
        submit("{\"name\":\"\",\"phone\":\"\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Nombre y teléfono son obligatorios."))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void conLaCotizacionApagadaElServidorLaRechaza() throws Exception {
        mockMvc.perform(put("/api/v1/admin/settings").contentType(MediaType.APPLICATION_JSON).content("""
                        {"heroTitle":"Ventanas y Aluminios SAS","heroSubtitle":"Sub","phone":"+57 317 846 3260",
                         "email":"ventanaluminio@hotmail.com","whatsappNumber":"","experienceYears":25,"quotingEnabled":false,
                         "aboutParagraph1":"a","aboutParagraph2":"b","aboutHighlight":"c"}
                        """))
                .andExpect(status().isOk());

        submit("{\"name\":\"Carlos\",\"phone\":\"300\"}")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("QUOTING_DISABLED"));
    }

    @Test
    void laBandejaFiltraPorEstadoOrdenaPorFechaYPagina() throws Exception {
        String first = createdId("{\"name\":\"Primera\",\"phone\":\"300\"}");
        createdId("{\"name\":\"Segunda\",\"phone\":\"300\"}");

        mockMvc.perform(patch("/api/v1/admin/quotes/" + first + "/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONTACTADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONTACTADA"));

        mockMvc.perform(get("/api/v1/admin/quotes").param("status", "CONTACTADA"))
                .andExpect(jsonPath("$.content[*].status", everyItem(is("CONTACTADA"))))
                .andExpect(jsonPath("$.content[*].id", hasItem(first)));

        mockMvc.perform(get("/api/v1/admin/quotes").param("size", "1").param("sort", "createdAt,desc"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Segunda"))
                .andExpect(jsonPath("$.size").value(1));

        mockMvc.perform(get("/api/v1/admin/quotes/" + first))
                .andExpect(jsonPath("$.history[0].fromStatus").value("NUEVA"))
                .andExpect(jsonPath("$.history[0].toStatus").value("CONTACTADA"));
    }

    @Test
    void ordenInvalidoSeRechaza() throws Exception {
        mockMvc.perform(get("/api/v1/admin/quotes").param("sort", "name,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    void eliminarEsLogicoYLaQuitaDeLaBandejaYDelContador() throws Exception {
        long newBefore = newQuotes();
        String id = createdId("{\"name\":\"Para borrar\",\"phone\":\"300\"}");
        org.assertj.core.api.Assertions.assertThat(newQuotes()).isEqualTo(newBefore + 1);

        mockMvc.perform(delete("/api/v1/admin/quotes/" + id)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/admin/quotes/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("La cotización no existe."));
        mockMvc.perform(get("/api/v1/admin/quotes").param("size", "100"))
                .andExpect(jsonPath("$.content[*].id", not(hasItem(id))));
        org.assertj.core.api.Assertions.assertThat(newQuotes()).isEqualTo(newBefore);
    }

    private ResultActions submit(String body) throws Exception {
        String ip = "10.1.0." + NEXT_IP.getAndIncrement();
        return mockMvc.perform(post("/api/v1/quotes").contentType(MediaType.APPLICATION_JSON).content(body)
                .with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                }));
    }

    private String createdId(String body) throws Exception {
        return JsonPath.read(submit(body).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(),
                "$.id");
    }

    private long total() throws Exception {
        return ((Number) JsonPath.read(mockMvc.perform(get("/api/v1/admin/quotes")).andReturn().getResponse()
                .getContentAsString(), "$.totalElements")).longValue();
    }

    private long newQuotes() throws Exception {
        return ((Number) JsonPath.read(mockMvc.perform(get("/api/v1/admin/dashboard")).andReturn().getResponse()
                .getContentAsString(), "$.newQuotes")).longValue();
    }
}
