package com.ventanasyaluminios.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestSizeLimitFilterTest {

    private final RequestSizeLimitFilter filter = new RequestSizeLimitFilter(new ObjectMapper());

    @Test
    void unaCotizacionMasGrandeQueElLimiteSeRechazaCon413SinLeerla() throws Exception {
        var request = new MockHttpServletRequest("POST", "/api/v1/quotes");
        request.setContent(new byte[(int) RequestSizeLimitFilter.PUBLIC_LIMIT + 1]);
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(413);
        assertThat(response.getContentAsString()).contains("PAYLOAD_TOO_LARGE");
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void sinContentLengthSeCortaAlLeerPorEncimaDelLimite() throws Exception {
        var request = new MockHttpServletRequest("POST", "/api/v1/auth/login") {
            @Override
            public long getContentLengthLong() {
                return -1;
            }
        };
        request.setContent(new byte[(int) RequestSizeLimitFilter.PUBLIC_LIMIT + 10]);
        var chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        var wrapped = (jakarta.servlet.http.HttpServletRequest) chain.getRequest();
        assertThatThrownBy(() -> wrapped.getInputStream().readAllBytes()).isInstanceOf(IOException.class);
    }

    @Test
    void elPanelTieneUnLimiteMayorParaElRespaldo() {
        assertThat(RequestSizeLimitFilter.limitFor("/api/v1/admin/backup")).isEqualTo(RequestSizeLimitFilter.DEFAULT_LIMIT);
        assertThat(RequestSizeLimitFilter.limitFor("/api/v1/quotes")).isEqualTo(RequestSizeLimitFilter.PUBLIC_LIMIT);
    }
}
