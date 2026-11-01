package com.ventanasyaluminios.exception;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Verifica que todo error llegue como { error, code } en español y sin detalles técnicos. */
@WebMvcTest(controllers = GlobalExceptionHandlerTest.FailingController.class)
@Import(GlobalExceptionHandlerTest.FailingController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void excepcionDeNegocioUsaSuEstadoYCodigo() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("La imagen no existe."))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void validacionDevuelveElMensajeDelCampo() throws Exception {
        mockMvc.perform(post("/test/validated").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El nombre es obligatorio."))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void jsonMalFormadoDevuelveMensajeEnEspanol() throws Exception {
        mockMvc.perform(post("/test/validated").contentType(MediaType.APPLICATION_JSON).content("{no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El contenido de la solicitud no es válido."))
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void rutaInexistenteDevuelve404ConFormatoUniforme() throws Exception {
        mockMvc.perform(get("/test/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void clienteQueNoAceptaJsonRecibe406SinConvertirseEnError500() throws Exception {
        mockMvc.perform(get("/test/ok").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void metodoNoPermitidoConservaSuEstado() throws Exception {
        mockMvc.perform(post("/test/not-found"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    void choqueDeIntegridadDevuelve409() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.error")
                        .value("Otro cambio se guardó al mismo tiempo. Actualiza la página e intenta de nuevo."));
    }

    @Test
    void edicionCruzadaDevuelve409() throws Exception {
        mockMvc.perform(get("/test/stale"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void textoConCaracterNuloSeRechazaCon400() throws Exception {
        mockMvc.perform(post("/test/validated").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"a\\u0000b\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void errorInesperadoNoExponeDetallesTecnicos() throws Exception {
        mockMvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Ocurrió un error inesperado. Intenta de nuevo."))
                .andExpect(content().string(not(containsString("NullPointerException"))));
    }

    record NamedRequest(@NotBlank(message = "El nombre es obligatorio.") String name) {
    }

    @RestController
    static class FailingController {

        @GetMapping("/test/not-found")
        void notFound() {
            throw new ResourceNotFoundException("La imagen no existe.");
        }

        @PostMapping("/test/validated")
        void validated(@Valid @RequestBody NamedRequest request) {
        }

        @GetMapping("/test/ok")
        NamedRequest ok() {
            return new NamedRequest("ok");
        }

        @GetMapping("/test/stale")
        void stale() {
            throw new ObjectOptimisticLockingFailureException(Object.class, 1L);
        }

        @GetMapping("/test/conflict")
        void conflict() {
            throw new DataIntegrityViolationException("duplicate key value violates unique constraint");
        }

        @GetMapping("/test/boom")
        void boom() {
            throw new NullPointerException("Cannot read properties of null");
        }
    }
}
