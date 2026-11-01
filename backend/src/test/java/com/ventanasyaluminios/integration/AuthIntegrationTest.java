package com.ventanasyaluminios.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ventanasyaluminios.TestcontainersConfiguration;
import com.ventanasyaluminios.entity.AdminUser;
import com.ventanasyaluminios.entity.Quote;
import com.ventanasyaluminios.repository.AdminUserRepository;
import com.ventanasyaluminios.repository.QuoteRepository;
import com.ventanasyaluminios.repository.QuoteStatusChangeRepository;
import com.ventanasyaluminios.security.SessionTokens;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/** Cadena real de Spring Security contra PostgreSQL: login, cookie, CSRF, contraseña temporal, revocación y logout. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestcontainersConfiguration.class)
class AuthIntegrationTest {

    private static final String EMAIL = "admin@empresa.com";
    private static final String TEMPORARY = "temporal-de-prueba-123";
    private static final String NEW_PASSWORD = "definitiva-de-prueba-456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminUserRepository adminRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private QuoteStatusChangeRepository statusChangeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AdminUser admin;

    @BeforeEach
    void createAdmin() {
        admin = adminRepository.save(new AdminUser(EMAIL, passwordEncoder.encode(TEMPORARY), true, Instant.now()));
    }

    @Test
    void sinSesionElPanelResponde401EnEspanolYLoPublicoSigueAbierto() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Tu sesión expiró, vuelve a ingresar."))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        // Una cookie vencida o inventada no afecta las rutas públicas.
        mockMvc.perform(get("/api/v1/site").cookie(new Cookie(SessionTokens.COOKIE_NAME, "basura")))
                .andExpect(status().isOk());
    }

    @Test
    void credencialesEquivocadasRespondenSinRevelarSiElCorreoExiste() throws Exception {
        login(EMAIL, "otra-clave-cualquiera")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Correo o contraseña incorrectos."));
        login("nadie@empresa.com", TEMPORARY)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Correo o contraseña incorrectos."));
    }

    @Test
    void flujoCompletoDeContrasenaTemporalCsrfRevocacionYCierre() throws Exception {
        MockHttpServletResponse loginResponse = login("ADMIN@empresa.com", TEMPORARY)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mustChangePassword").value(true))
                .andReturn().getResponse();
        String setCookie = sessionSetCookie(loginResponse);
        assertThat(setCookie).contains("HttpOnly").contains("SameSite=Lax").contains("Path=/api")
                .doesNotContain("Max-Age=0");
        Cookie temporarySession = loginResponse.getCookie(SessionTokens.COOKIE_NAME);

        // Con contraseña temporal no entra al panel.
        mockMvc.perform(get("/api/v1/admin/dashboard").cookie(temporarySession))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));

        // Sin token CSRF, el cambio se rechaza.
        mockMvc.perform(post("/api/v1/auth/change-password").cookie(temporarySession)
                        .contentType(MediaType.APPLICATION_JSON).content(passwordChange()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CSRF_INVALID"));

        MockHttpServletResponse changed = withCsrf(post("/api/v1/auth/change-password"), temporarySession)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mustChangePassword").value(false))
                .andReturn().getResponse();
        Cookie session = changed.getCookie(SessionTokens.COOKIE_NAME);

        // La sesión temporal quedó revocada; la nueva entra al panel.
        mockMvc.perform(get("/api/v1/auth/me").cookie(temporarySession)).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/admin/dashboard").cookie(session)).andExpect(status().isOk());

        // El historial registra quién cambió el estado de una cotización.
        Quote quote = quoteRepository.save(newQuote());
        withCsrf(patch("/api/v1/admin/quotes/" + quote.getId() + "/status"), session, "{\"status\":\"CONTACTADA\"}")
                .andExpect(status().isOk());
        assertThat(statusChangeRepository.findByQuoteIdOrderByChangedAtAscIdAsc(quote.getId()))
                .singleElement().satisfies(change -> assertThat(change.getChangedBy()).isEqualTo(admin.getId()));

        // Cerrar sesión borra la cookie e invalida el token aunque alguien lo haya copiado.
        MockHttpServletResponse loggedOut = withCsrf(post("/api/v1/auth/logout"), session, "")
                .andExpect(status().isNoContent())
                .andReturn().getResponse();
        assertThat(sessionSetCookie(loggedOut)).contains("Max-Age=0");
        mockMvc.perform(get("/api/v1/admin/dashboard").cookie(session)).andExpect(status().isUnauthorized());
    }

    @Test
    void unTokenFirmadoConOtraClaveSeRechaza() throws Exception {
        String forged = "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ2ZW50YW5hc3lhbHVtaW5pb3MiLCJzdWIiOiIxIiwidmVyIjowLCJleHAiOjQxMDI0NDQ4MDB9."
                + "c2lnbmF0dXJhLWZhbHNh";
        mockMvc.perform(get("/api/v1/admin/dashboard").cookie(new Cookie(SessionTokens.COOKIE_NAME, forged)))
                .andExpect(status().isUnauthorized());
    }

    private static String sessionSetCookie(MockHttpServletResponse response) {
        return response.getHeaders(HttpHeaders.SET_COOKIE).stream()
                .filter(header -> header.startsWith(SessionTokens.COOKIE_NAME + "="))
                .findFirst()
                .orElseThrow();
    }

    private ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}")
                .with(request -> {
                    request.setRemoteAddr("10.20.0." + Math.abs(email.hashCode() % 200));
                    return request;
                }));
    }

    private String passwordChange() {
        return "{\"currentPassword\":\"" + TEMPORARY + "\",\"newPassword\":\"" + NEW_PASSWORD + "\"}";
    }

    private ResultActions withCsrf(MockHttpServletRequestBuilder request, Cookie session) throws Exception {
        return withCsrf(request, session, passwordChange());
    }

    /** Como el navegador: pide el token CSRF (cookie XSRF-TOKEN) y lo devuelve en el encabezado X-XSRF-TOKEN. */
    private ResultActions withCsrf(MockHttpServletRequestBuilder request, Cookie session, String body) throws Exception {
        Cookie xsrf = mockMvc.perform(get("/api/v1/auth/me").cookie(session)).andReturn().getResponse()
                .getCookie("XSRF-TOKEN");
        assertThat(xsrf).isNotNull();
        return mockMvc.perform(request.cookie(session, xsrf).header("X-XSRF-TOKEN", xsrf.getValue())
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private static Quote newQuote() {
        Quote quote = new Quote(UUID.randomUUID(), Instant.now(), "10.0.0.1");
        quote.setName("Carlos");
        quote.setPhone("300");
        return quote;
    }
}
